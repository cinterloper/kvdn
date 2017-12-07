package net.iowntheinter.kvdn.storage.kv.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX

import net.iowntheinter.kvdn.storage.kv.TXKV
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.KvdnSession

import java.security.MessageDigest

/**
 * Created by grant on 11/19/15.
 */
@TypeChecked
@CompileStatic
class KvTx extends KvdnTX implements TXKV {
    public KVData D
    public KVData M

    boolean finished


    KvTx(String sa, UUID txid, KvdnSession session, Vertx vertx) {
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as KvdnSession
        this.txid = txid
        this.strAddr = sa
        //doing it this way avoids a wierd compile static error
        // Type 'java/lang/Object' (current frame, stack[6]) is not assignable to 'java/lang/String'
        String classname = this.getClass().getName().toString()
        String ID = classname + ":${strAddr}" //
        this.logger = new LoggerFactory().getLogger(ID)
        //end wierd hack
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
        this.D = session.D as KVData
        this.M = session.D as KVData

    }

    @Override
    void snapshot() {
        throw new Exception("unimplemented")
    }

    @Override
    @TypeChecked
    void submit(String content, Handler cb) {
        startTX(TXTYPE.KV_SUBMIT, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content.toString()).getBytes()).encodeHex().toString()
                    map.put(key, content, { AsyncResult resSubmit ->
                        if (resSubmit.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishTx(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                    //cb.handle(Future.succeededFuture(key))
                                })
                            })

                        } else {
                            bailTx(Future.failedFuture(resSubmit.cause()),this,cb)
                        }
                    })
                } else {
                    bailTx(Future.failedFuture(res.cause()),this,cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void set(String key, String content, Handler cb) {
        startTX(TXTYPE.KV_SET, [keys: [key]], {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.put(key, content, { AsyncResult resSet ->
                        if (resSet.succeeded()) {
                            keyprov.setKey(strAddr, key, {
                                (this.session as KvdnSession).finishTx(this, {
                                    eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            bailTx(Future.failedFuture(resSet.cause()),this,cb)
                        }
                    })
                } else {
                    bailTx(Future.failedFuture(res.cause()),this,cb)

                }
            })
        })
    }


    @Override
    @TypeChecked
    void get(String key, Handler cb) {
        startTX(TXTYPE.KV_GET, [keys: [key]], {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    AsyncMap map = res.result()
                    map.get(key, { AsyncResult<String> resGet ->
                        if (resGet.succeeded()) {
                            (this.session as KvdnSession).finishTx(this, {
                                cb.handle(Future.succeededFuture(resGet.result()))
                            })
                        } else {
                            bailTx(Future.failedFuture(resGet.cause()),this,cb)
                        }
                    })
                } else {
                    bailTx(Future.failedFuture(res.cause()),this,cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void del(String key, Handler cb) {
        startTX(TXTYPE.KV_DEL, [keys: [key]], {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    AsyncMap map = res.result()
                    map.remove(key, { AsyncResult resDel ->
                        if (resDel.succeeded()) {
                            keyprov.deleteKey(strAddr, key, {
                                (this.session as KvdnSession).finishTx(this, {
                                    eb.publish("_KVDN_-${strAddr}", new JsonObject().put('key', key))
                                    cb.handle(Future.succeededFuture(key))
                                })
                            })
                        } else {
                            bailTx(Future.failedFuture(resDel.cause()),this,cb)
                        }
                    })
                } else {
                    bailTx(Future.failedFuture(res.cause()),this,cb)
                }
            })
        })
    }

    @Override
    @TypeChecked
    void getKeys(Handler cb) {
        startTX(TXTYPE.KV_KEYS, {
            keyprov.getKeys(this.strAddr, { AsyncResult asyncResult -> //@FixMe this should be a real AsyncResult
                (this.session as KvdnSession).finishTx(this, {
                    cb.handle(asyncResult)
                })
            })
        })
    }

    @Override
    @TypeChecked
    void size(Handler<AsyncResult<Integer>> cb) {
        startTX(TXTYPE.KV_SIZE, {
            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    AsyncMap map = res.result()
                    map.size({ AsyncResult<Integer> resSize ->
                        if (resSize.succeeded()) {
                            logger.trace("got size": resSize.result())
                            (this.session as KvdnSession).finishTx(this, {
                                cb.handle(Future.succeededFuture(resSize.result()))
                            })
                        } else {
                            bailTx(Future.failedFuture(resSize.cause()),this,cb)
                        }
                    })
                } else {
                    bailTx(Future.failedFuture(res.cause()),this,cb)
                }
            })
        })
    }

}