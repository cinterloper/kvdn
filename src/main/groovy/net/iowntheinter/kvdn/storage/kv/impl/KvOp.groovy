package net.iowntheinter.kvdn.storage.kv.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnResult
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.query.DBObjectMgr
import net.iowntheinter.kvdn.storage.kv.KVDNRefrence
import net.iowntheinter.kvdn.storage.kv.KVOperation
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.meta.types.ValueType

import java.security.MessageDigest
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by grant on 11/19/15.
 */
@CompileStatic
class KvOp extends KvdnOperation implements KVOperation {
    public KVData D
    public KVData M
    private final KvdnSession session
    enum opflags {
        BULK
    }
    public List<opflags> flags = []


    boolean finished

    /*
    Assumptions:
    this is always executed by a single thred
     - all multithreaded programs should use the service client which should be thread safe
     - if we where to make this really a transaction, we would need to acquire a lock on all resources it depends on
       - read some stuff, do some thinking, write some stuff?
       - batch operations?

     alternitivly if it remains operation based we can tie the key to the TX
      - push key through to hooks this way
       - maybe this should be renamed into KvOp

      - create another level of abstraction for transactions / batch operations?

       - this is probably the way to go


     */

    String TypeCast(KvOp tx, Object o) {
        if (o instanceof String) {
            return (String) o
        }
        return o.toString()
    } //this should call casting the String to its origin type

    KvOp(String sa, String mimeType = VALUETYPE.STRING, UUID txid, KvdnSession session, Vertx vertx) {
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as KvdnSession
        this.txid = txid
        this.strAddr = sa
        //doing it this way avoids a wierd compile static error
        // Type 'java/lang/Object' (current frame, stack[6]) is not assignable to 'java/lang/String'
        String classname = this.getClass().getName().toString()
        String ID = "$classname:${strAddr}" //
        this.logger = new LoggerFactory().getLogger(ID)
        //end wierd hack
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
        this.D = session.D
        this.M = session.D

    }
//
//    ConcurrentHashMap<String,ValueType> OperationTypes = [:] //for keys in this tx that have write operations, store their types

    Lock execute = new ReentrantLock()

    @Override
    void snapshot() {
        throw new Exception("unimplemented")
    }


    @Override
//    @TypeChecked
    void submit(String content,
                Map<String, Object> options = ([hashAlgo: "MD5", truncate: 0, valueType: VALUETYPE.STRING] as Map<String, Object>),
                Handler<AsyncResult<String>> cb) {
        //its dangerous to do work (hashing) before confirming anything.
        //@todo : should we implement this here? maybe hashing should be a layer higher
        String hashAlgo = options["hashAlgo"] as String
        int truncate = options["truncate"] as int
        try {
            assert options.containsKey("valueType")
        } catch (Exception e) {
            logger.error("valueType must be specified when passing an options map ${e.message}")
        }
        this.valueType = options["valueType"] as String
        this.options = options
        String key = MessageDigest.getInstance(hashAlgo).digest((content.toString()).getBytes()).encodeHex().toString()
        if (truncate != 0) { //truncate the hash
            key = key.take(truncate).toString()
        }

            startOperation(this.valueType, TXTYPE.KV_SUBMIT, key, content, {
                D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                    if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                        AsyncMap map = res.result()

                        map.put(key, content, { AsyncResult resSubmit ->
                            if (resSubmit.succeeded()) {
                                logger.trace("submit transaction internal success with hash ${key} content ${content}")
                                keyprov.setKey(strAddr, key, {
                                    (this.session as KvdnSession).finishOp(this, {

                                        eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                        cb.handle(Future.succeededFuture(key))
                                        //cb.call(Future.succeededFuture(key))
                                    })
                                })

                            } else {
                                abortOperation(resSubmit, this, cb)
                            }
                        })
                    } else {
                        abortOperation(res, this, cb)
                    }
                })
            })
        }


    @Override
//    @TypeChecked
    void set(String key, String content,
             Map<String, Object> options = ([valueType: VALUETYPE.STRING] as Map<String, Object>),
             Handler<AsyncResult<String>> cb) {

        if (!options.containsKey('valueType')){
            logger.warn("in KvOp.set for ${key} on ${strAddr}, the options map passed does not contain an explicit valueType, defaulting to STRING")
            options.valueType=VALUETYPE.STRING
        }

//
//        try {
//            assert options.containsKey("valueType")
//        } catch (Exception e) {
//            logger.error("valueType must be specified when passing an options map")
//        }
        this.valueType = options["valueType"] as String
        this.options = options

        startOperation(this.valueType, TXTYPE.KV_SET, [keys: [key]], key, content, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.put(key, content, { AsyncResult resSet ->
                        if (resSet.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishOp(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            abortOperation(resSet, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)

                }
            })
        })
    }

    void mkref(String key, String target, KVDNRefrence.reftype type,
               Map<String, Object> options = ([:] as Map<String, Object>),
               Handler<AsyncResult> cb) {
        //create a ref object and attach it to @key
        JsonObject ref = new JsonObject(options)
        ref.put("target", target)
        ref.put("type", type.toString())
        set(key, ref.toString(), [valueType: VALUETYPE.REFRENCE] as Map<String, Object>, { AsyncResult res ->
            cb(res)
        })

    }

    void setBulk(Map content,
                 Map<String, Object> options = ([typeMap: null] as Map<String, Object>),
                 Handler<AsyncResult> cb) {
        //although all values are lowered to strings, a typeMap can be passed in the form
        //[ key:type ] where type is a @KVDNOperation.VALUETYPE
        //this gets set in a metadata map that shadows this map
        //nested data can contain pointers to additional metadata maps
        //the underling storage engine can leverage this type info
        //with no typeMap we can attempt to infer a typeMap

        try {
            assert options.containsKey("valueType")
        } catch (Exception e) {
            logger.error("valueType must be specified when passing an options map")
        }
        this.valueType = options["valueType"] as String
        this.options = options
        Map<String, VALUETYPE> typeMap
        if (options.typeMap != null)
            typeMap = options.typeMap as Map<String, VALUETYPE>
        else
            typeMap = [:]

        ArrayList<Future> futures = new ArrayList<>()

        for (entry in content) {
            String k = entry.key
            String v
            if (entry.value instanceof Map) {
                v = new JsonObject(entry.value as Map)
                typeMap[k] = VALUETYPE.JSON
            } else if (entry.value instanceof ArrayList) {
                v = new JsonArray(entry.value as ArrayList)
                typeMap[k] = VALUETYPE.JSON_ARRAY
            } else if (entry.value instanceof Number) {
                v = entry.value.toString()
                typeMap[k] = VALUETYPE.NUMBER
            } else if (entry.value instanceof Boolean) {
                v = entry.value.toString()
                typeMap[k] = VALUETYPE.BOOL
            } else if (entry.value == null) {
// @todo should we formally support nulls?
                v = entry.value.toString()
                typeMap[k] = VALUETYPE.NULL
            } else {
                v = entry.value.toString()
                typeMap[k] = VALUETYPE.STRING
            }

//@todo good place to use transactions
            Future f = Future.future()
            futures.push(f)
            set(k as String, v, [valueType: typeMap[k] as Object], { AsyncResult resPut ->
                if (resPut.succeeded()) {
                    f.succeeded()
                    f.complete()
                } else {
                    logger.error(resPut)
                    f.fail(resPut.cause())
                    f.complete()
                }
            })
        }
        CompositeFuture c = CompositeFuture.all(futures)
        c.setHandler(cb)

    }

    void replace(String key, String content,
                 Map<String, Object> options = ([valueType: VALUETYPE.STRING] as Map<String, Object>),
                 Handler<AsyncResult<String>> cb) {
        try {
            assert options.containsKey("valueType")
        } catch (Exception e) {
            logger.error("valueType must be specified when passing an options map")
        }
        this.valueType = options["valueType"] as String
        this.options = options

        startOperation(this.valueType, TXTYPE.KV_SET, [keys: [key]], key, content, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.replace(key, content, { AsyncResult resSet ->
                        if (resSet.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishOp(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            abortOperation(resSet, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)

                }
            })
        })
    }

    void put(String key, String content,
             Map<String, Object> options = ([valueType: VALUETYPE.STRING] as Map<String, Object>),
             Handler<AsyncResult<String>> cb) {
        logger.trace("called put, is alias for set, calling set")
        set(key, content, options, cb)
    }


    @Override
//    @TypeChecked
    void putIfAbsent(String key, String content,
                     Map<String, Object> options = ([valueType: VALUETYPE.STRING] as Map<String, Object>),
                     Handler<AsyncResult<String>> cb) {
        logger.trace("putIfAbsent")
        try {
            assert options.containsKey("valueType")
        } catch (Exception e) {
            logger.error("valueType must be specified when passing an options map")
        }
        this.valueType = options["valueType"] as String
        this.options = options

        startOperation(this.valueType, TXTYPE.KV_SET, [keys: [key]], key, content, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.putIfAbsent(key, content, { AsyncResult resSet ->
                        if (resSet.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishOp(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            abortOperation(resSet, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)

                }
            })
        })
    }

    @Override
    @TypeChecked
    void replaceIfPresent(String key,
                          String expectedContent,
                          String content,
                          Map<String, Object> options = ([valueType: VALUETYPE.STRING, expectedHash: false] as Map<String, Object>),
                          Handler<AsyncResult<String>> cb) {
        try {
            assert options.containsKey("valueType")
        } catch (Exception e) {
            logger.error("valueType must be specified when passing an options map")
        }
        this.valueType = options["valueType"] as String
        this.options = options

        startOperation(this.valueType, TXTYPE.KV_SET, [keys: [key]], key, content, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.replaceIfPresent(key, expectedContent, content, { AsyncResult resSet ->
                        if (resSet.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishOp(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            abortOperation(resSet, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)

                }
            })
        })
    }

    @Override
    @TypeChecked
    void get(String key,
             Map<String, Object> options = ([] as Map<String, Object>),
             Handler<AsyncResult<String>> cb) {
        this.options = options

        startOperation(TXTYPE.KV_GET, [keys: [key]], {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    AsyncMap map = res.result()
                    map.get(key, { AsyncResult<String> resGet ->
                        //@todo I think we need to encode the type with the value
                        //@todo pack/unpack vlaue
                        if (resGet.succeeded()) {
                            (this.session as KvdnSession).finishOp(this, {
                                logger.trace("take transaction reult internal ${resGet.result()}")
                                cb.handle(new KvdnResult(TypeCast(this, resGet.result()), ValueType.TEXT_PLAIN))
                            })
                        } else {
                            abortOperation(resGet, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void clear(Map<String, Object> options = ([] as Map<String, Object>),
               Handler<AsyncResult<Boolean>> cb) {
        this.options = options

        startOperation(TXTYPE.KV_DEL, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.clear({ AsyncResult resClear ->
                        if (resClear.succeeded()) {
                            (this.session as KvdnSession).finishOp(this, {
                                eb.publish("_KVDN_X${strAddr}", new JsonObject())
                                cb.handle(Future.succeededFuture(true))
                            })
                        } else {
                            abortOperation(resClear, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void del(String key,
             Map<String, Object> options = ([] as Map<String, Object>),
             Handler<AsyncResult<String>> cb) {
        this.options = options

        startOperation(TXTYPE.KV_DEL, [keys: [key]], {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.remove(key, { AsyncResult resDel ->
                        if (resDel.succeeded()) {
                            keyprov.deleteKey(strAddr, key, {
                                (this.session as KvdnSession).finishOp(this, {
                                    eb.publish("_KVDN_-${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            abortOperation(resDel, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void getKeys(Map<String, Object> options = ([] as Map<String, Object>),
                 Handler<AsyncResult<Set<String>>> cb) {
        this.options = options

        startOperation(TXTYPE.KV_KEYS, {
            keyprov.getKeys(this.strAddr, { AsyncResult<Set<String>> asyncResult ->
                if (asyncResult.succeeded())
                    (this.session as KvdnSession).finishOp(this, {
                        logger.trace("result of getKeys ${asyncResult.result()}")
                        cb.handle(asyncResult)
                    })
                else
                    abortOperation(asyncResult, this, cb)
            })
        })
    }

    @Override
    @TypeChecked
    void size(Map<String, Object> options = ([] as Map<String, Object>),
              Handler<AsyncResult<Integer>> cb) {
        this.options = options

        startOperation(TXTYPE.KV_SIZE, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    AsyncMap map = res.result()
                    map.size({ AsyncResult<Integer> resSize ->
                        if (resSize.succeeded()) {
                            logger.trace("got size": resSize.result())
                            (this.session as KvdnSession).finishOp(this, {
                                cb.handle(Future.succeededFuture(resSize.result()))
                            })
                        } else {
                            abortOperation(resSize, this, cb)
                        }
                    })
                } else {
                    abortOperation(res, this, cb)
                }
            })
        })
    }

}