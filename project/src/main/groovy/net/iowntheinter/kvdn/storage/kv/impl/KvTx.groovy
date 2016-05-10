package net.iowntheinter.kvdn.storage.kv.impl

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.crdts.sets.ORSet
import net.iowntheinter.kvdn.storage.kv.TSKV

import java.security.MessageDigest
//import com.hazelcast.replicatedmap.impl.record.VectorClockTimestamp

/**
 * Created by grant on 11/19/15.
 */
class KvTx implements TSKV {
    SharedData sd;
    def logger
    EventBus eb;
    def strAddr
    ORSet keys
    Map KeySets
    Kryo serializer

    def KvTx(String sa, session, kryo, vertx) {
        // keys = new ORSet()
        strAddr = sa
        serializer = kryo
        logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
        /*  sd.getClusterWideMap('k_NamesToRoots', { AsyncResult ar ->
              if (!ar.failed())
      //            NamesToRoots = ar.result() as AsyncMap
          })
  */
        KeySets = session.keysets

    }

    def getMap(String name, Closure cb){

    }

    def peerUpdateHdlr(ORSet update, uaddr) {
        ORSet keys = KeySets.get(uaddr) as ORSet
        if(keys == null)
            keys = new ORSet()
        keys.merge(update)
        KeySets.put(uaddr, keys)
    }
    def peerUpdateHdlr(ORSet update, uaddr,cb) {
        ORSet keys = KeySets.get(uaddr) as ORSet
        if(keys == null)
            keys = new ORSet()
        keys.merge(update)
        KeySets.put(uaddr, keys)
        cb(new JsonObject().put("result", new JsonArray(keys.get().toList())))
    }
    def bailTx(ctx) { //something went wrong, bail out of the transaction

    }

    void snapshot() {
        // each
        //maybe we can do this in a non blocking way by having copy-on-write snapshots
        //rather, on snapshot, instantiate a new map
        // gets would have to castcade through the generations,
        // if the new map dosent contain a key, test the older map, etc

        //protocol: all _kvdn_ keys are private metadata on a map.
        //normal _user_
        //example
        // _kvdn_root:false
        // _kvdn_parent:00b9ce90-b772-475a-8694-139cfc999cad


    }

    public void lookupKeyTop(mapAddr, kName, cb) {

    }
    // default to unlimited depth
    public void lookupKeyRecurse(mapAddr, kName, def depth = 0, cb) {

    }

    public void createMap(strAddr, cb) {
        def exists = false
        def id = null;
        getMapRootId(strAddr, { Map ar ->
            if (ar['rootId'] != null) {
                exists = true
                id = ar['rootId']
            }
        })
        if (exists) {
            cb([error: "exists", mapId: id])
        } else {
            id = UUID.randomUUID()
            NamesToRoots.putIfAbsent(strAddr, id, { AsyncResult ar ->
                if (ar.succeeded()) {

                }
            })
        }

    }

    public void getMapFromId(mapId, cb) {
        def resp = [error: null]
        sd.getClusterWideMap('k_NamesToRoots', { AsyncResult ar ->
            if (!ar.failed())
                resp['map'] = ar.result() as AsyncMap
            else
                resp['error'] = ar.cause()
        })
    }
//get the current root map for a storage address, and pass it to the callback
    def getMapRootId(strAddr, cb) {
        String rootId
        try {
            NamesToRoots.get(strAddr, { AsyncResult ar ->
                if (!ar.failed()) {
                    rootId = ar.result() as String
                    cb([error: null, rootId: rootId])
                } else
                    cb([error: "could not lookup root map for ${straddr}", rootId: null])

            })
        } catch (getCurMapHandle) {
        }
    }


    def keyReqHandler = { message ->
        logger.trace("Got a remote key req: ${message.body().strAddr}")
        Map keyReqCtx = message.body()
        def strAddr = keyReqCtx["strAddr"]
        def baos = new ByteArrayOutputStream();
        def out = new Output(baos);
        def keys = KeySets.get(strAddr)
        serializer.writeObjectOrNull(out, keys, ORSet.class)
        def replContents = baos.toByteArray()
        message.reply(replContents)
        logger.trace("replyed to keyreq ")
    }

    void submit(content, cb) {
        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                def String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content).getBytes()).encodeHex().toString()
                map.put( key , content, { resPut ->
                    if (resPut.succeeded()) {
                        def atmt = KeySets.get(strAddr)
                        if (atmt != null)
                            keys = atmt as ORSet
                        if (keys == null) {
                            logger.trace("setting up keyset for ${strAddr} ")
                            keys = new ORSet()
                            eb.consumer("keyreq_${strAddr}").handler(keyReqHandler).completionHandler({ ar ->
                                logger.trace("completed setting up handler for ${strAddr} + ${ar.cause()}")
                            })
                        }
                        keys.add(key)
                        KeySets.put(strAddr, keys)

                        //notify those subscribing to the map that a key has ben updated
                        eb.publish("+_${strAddr}", key)
                        def baos = new ByteArrayOutputStream();
                        def out = new Output(baos);
                        serializer.writeObjectOrNull(out, keys, ORSet.class)

                        eb.publish("_keysync_${strAddr}", baos.toByteArray())
                        logger.trace("set:${strAddr}:${key}");
                        cb([result: resPut.result().toString(),key:key, error: null])
                    } else {
                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                cb([result: null, error: res.cause()])
            }
        })
    }
    void set(key, content, cb) {
        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        def atmt = KeySets.get(strAddr)
                        if (atmt != null)
                            keys = atmt as ORSet
                        if (keys == null) {
                            logger.trace("setting up keyset for ${strAddr} ")
                            keys = new ORSet()
                            eb.consumer("keyreq_${strAddr}").handler(keyReqHandler).completionHandler({ ar ->
                                logger.trace("completed setting up handler for ${strAddr} + ${ar.cause()}")
                            })
                        }
                        keys.add(key)
                        KeySets.put(strAddr, keys)

                        //notify those subscribing to the map that a key has ben updated
                        eb.publish("+_${strAddr}", key)
                        def baos = new ByteArrayOutputStream();
                        def out = new Output(baos);
                        serializer.writeObjectOrNull(out, keys, ORSet.class)

                        eb.publish("_keysync_${strAddr}", baos.toByteArray())
                        logger.trace("set:${strAddr}:${key}");
                        cb([result: resPut.result().toString(), error: null])
                    } else {
                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                cb([result: null, error: res.cause()])
            }
        })
    }



    Object getKeys(cb) {
        def atmt = KeySets.get(strAddr)
        if (atmt != null)
            keys = atmt as ORSet
        if (keys == null) {

            eb.send("keyreq_${strAddr}", new JsonObject([strAddr: strAddr]), { resp ->
                if(resp.result()) {
                    def data = resp.result().body()
                    logger.trace("got response from keyreq_${strAddr}: err if any: ${resp.cause()}  ")
                    def inp = new Input(data as byte[])
                    peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class), strAddr, cb)
                }
                else{
                    logger.error("no response from key request")
                    cb(new JsonObject().put("error","no response from key request"))
                }
            })
        } else
            cb(new JsonObject().put("result", new JsonArray(keys.get().toList())))

    }

    void get(key, cb) {

        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.get(key, { resGet ->
                    if (resGet.succeeded()) {
                        logger.trace("get:${strAddr}:${key}");
                        cb([result: resGet.result().toString(), error: null])
                    } else {
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                cb([result: null, error: res.cause()])
            }
        })
    }


    void del(key, cb) {
        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.remove(key, { resDel ->
                    if (resDel.succeeded()) {
                        keys = KeySets.get(strAddr) as ORSet
                        try{
                            keys.remove(key)
                            KeySets.put(strAddr, keys)
                            def baos = new ByteArrayOutputStream();
                            def out = new Output(baos);
                            serializer.writeObjectOrNull(out, keys, ORSet.class)
                            eb.publish("_keysync_${strAddr}", baos.toByteArray())
                            logger.trace("del:${strAddr}:${key}");
                        }catch(e){
                            logger.warn(e)
                        }

                        cb([result: resDel.result().toString(), error: null])

                    } else {
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                cb([result: null, error: res.cause()])
            }
        })
    }
}


