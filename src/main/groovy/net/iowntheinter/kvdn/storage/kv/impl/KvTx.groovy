package net.iowntheinter.kvdn.storage.kv.impl

import io.vertx.core.Vertx
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.TXKV
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap

import java.security.MessageDigest

/**
 * Created by grant on 11/19/15.
 */

class KvTx implements TXKV {
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    def  keyprov
    def Vertx vertx
    def D = new _data_impl()
    def session
    class _data_impl {
        SharedData sd;
        Vertx vertx
        String name

        void getMap(KvTx txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            if (vertx.isClustered()) {  //vertx cluster mode
                this.sd = vertx.sharedData()
                sd.getClusterWideMap("${name}", cb)
                logger.trace("starting clustered kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
            } else {                    // vertx local mode
                logger.trace("starting local kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
                cb(Future.succeededFuture(new shimAsyncMap(vertx, name)))
            }
        }
    }

    def KvTx(String sa, kvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session
        strAddr = sa
        logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
    }

    @Override
    Object bailTx(Object context) {
        return null
    }

    @Override
    void snapshot() {}

    @Override
    void submit(content, cb) {
        D.getMap(this, { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                def String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content.toString()).getBytes()).encodeHex().toString()

                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {

                        keyprov.addKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key',key))
                            cb([result: resPut.result().toString(), key: key, error: null])
                        })

                    } else {
                        logger.error("an error occured in a submit() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")

                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                logger.error("an error occured in a submit() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")

                cb([result: null, error: res.cause()])
            }
        })
    }

    @Override
    void set(String key, content, cb) {
        D.getMap(this, { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        keyprov.addKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key',key))
                            cb([result: resPut.result().toString(), key: key, error: null])
                        })
                    } else {
                        logger.error("an error occured in a set() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")
                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                logger.error("an error occured in a set() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")
                cb([result: null, error: res.cause()])
            }
        })
    }

    @Override
    void getKeys(cb) {
        keyprov.getKeys(strAddr, { Map asyncResult ->

            cb(asyncResult)
        })
    }

    @Override
    void get(String key, cb) {
        D.getMap(this, { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.get(key, { resGet ->
                    if (resGet.succeeded()) {
                        logger.trace("get:${strAddr}:${key}");
                        cb([result: resGet.result().toString(), error: null])
                    } else {
                        logger.error("an error occured in a get() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                cb([result: null, error: res.cause()])
            }
        })
    }

    @Override
    void del(String key, cb) {
        D.getMap(this, { res ->
            if (res.succeeded()) {
                def AsyncMap map = res.result();
                map.remove(key, { resDel ->
                    if (resDel.succeeded()) {
                        keyprov.deleteKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_-${strAddr}", new JsonObject().put('key'))
                            cb([result: resDel.result().toString(), key: key, error: null])
                        })
                        cb([result: resDel.result().toString(), error: null])

                    } else {
                        logger.error("an error occured in a del() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")

                        cb([result: null, error: resDel.cause()])
                    }
                })
            } else {
                logger.error("an error occured in a del() transaction on key: ${key} straddr: ${strAddr} session: ${this.session} ")

                cb([result: null, error: res.cause()])
            }
        })
    }
}