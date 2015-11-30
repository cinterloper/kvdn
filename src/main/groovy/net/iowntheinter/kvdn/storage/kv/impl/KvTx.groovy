package net.iowntheinter.kvdn.storage.kv.impl

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import io.vertx.core.AsyncResult
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.eventbus.EventBus
import io.vertx.groovy.core.shareddata.AsyncMap
import io.vertx.groovy.core.shareddata.LocalMap
import io.vertx.groovy.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.TSKV
import net.iowntheinter.crdts.sets.ORSet
import net.iowntheinter.crdts.CRDT;

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

    def KvTx(String sa, Map keysets, s, vertx) {
        // keys = new ORSet()
        strAddr = sa
        serializer = s
        logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
        /*  sd.getClusterWideMap('k_NamesToRoots', { AsyncResult ar ->
              if (!ar.failed())
      //            NamesToRoots = ar.result() as AsyncMap
          })
  */
        KeySets = keysets

    }

    def peerUpdateHdlr(ORSet update) {
        keys = KeySets.get(strAddr) as ORSet
        keys.merge(update)
        KeySets.put(strAddr, keys)
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
        sd.getSync
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

    def acquireLock(strAddr, cb) {
        acquireLockResources(strAddr as String, { Map resrc ->
            if (resrc['error'] != null) {
                def c = resrc['ctr']
                def l = resrc['lock']
                next = c + 1

                try {
                    worked = txCounter.compareandset(c, next)
                } catch (failedException) {
                    //         bail out of this transaction
                }
                //no one can do shit till i release this, if blocked i should listen for an unlock event
                //possiably warn if blocked for to long
            }
        })


    }

    def acquireLockResources(String mName, cb) {
        def mapLock, mapTxCtr
        try {
            sd.getLock(mName, { AsyncResult lar ->
                if (!lar.failed()) {
                    mapLock = lar.result()
                    sd.getCounter(mName, { AsyncResult car ->
                        if (!car.failed()) {
                            mapTxCtr = car.result()
                            cb([lck: mapLock, ctr: mapTxCtr, error: null])
                        } else {
                            cb([lck: null, ctr: null, error: car.cause()])
                        }
                    })
                }
            })
        } catch (e) {
            log.error(e)
        }


    }

    void set(key, content, cb) {
        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        keys = KeySets.get(strAddr) as ORSet
                        if (keys == null) {
                            keys = new ORSet()
                            eb.consumer("keysync_" + strAddr, { message -> //listen for updates on this keyset
                                ORSet foreign;
                                def inp = new Input(message.body() as byte[])
                                peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class))
                            })
                        }
                        keys.add(key)
                        KeySets.put(strAddr, keys)

                        //notify those subscribing to the map that a key has ben updated
                        eb.publish("+_${strAddr}", key)
                        def baos = new ByteArrayOutputStream();
                        def out = new Output(baos);
                        serializer.writeObjectOrNull(out, keys, ORSet.class)

                        eb.publish("keysync_${strAddr}", baos.toByteArray())
                        logger.info("set:${strAddr}:${key}");
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
        keys = KeySets.get(strAddr) as ORSet
        cb([result:keys.get(),error:null])
    }

    void get(key, cb) {

        sd.getClusterWideMap("${strAddr}", { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.get(key, { resGet ->
                    if (resGet.succeeded()) {
                        logger.info("get:${strAddr}:${key}");
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
                        keys.remove(key)
                        KeySets.put(strAddr, keys)
                        def baos = new ByteArrayOutputStream();
                        def out = new Output(baos);
                        serializer.writeObjectOrNull(out, keys, ORSet.class)
                        eb.publish("keysync_${strAddr}", baos.toByteArray())
                        logger.info("del:${strAddr}:${key}");
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


