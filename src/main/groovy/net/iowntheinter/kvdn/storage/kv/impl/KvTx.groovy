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
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kv.TXKV
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap
import net.iowntheinter.kvdn.storage.kvdnSession

import java.security.MessageDigest

/**
 * Created by grant on 11/19/15.
 */

class KvTx extends kvdnTX implements TXKV {
    def D

    private class _data_impl {
        SharedData sd;
        Vertx vertx
        String sa
        
        _data_impl(Vertx v, String sa){
            this.vertx = v
            this.sa = sa
            this.sd = vertx.sharedData()
        }
        
        void getMap(cb) {
            if (vertx.isClustered()) {  //vertx cluster mode
                sd.getClusterWideMap("${sa}", cb)
                logger.trace("starting clustered kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
            } else {                    // vertx local mode
                logger.trace("starting local kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
                cb(Future.succeededFuture(new shimAsyncMap(vertx, sa)))
            }
        }
    }

    def KvTx(String sa, UUID txid, kvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as kvdnSession
        this.txid = txid
        this.strAddr = sa
        this.logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
        this.D = new _data_impl(vertx, this.strAddr)
    }
    
    @Override
    void snapshot() {
        throw new Exception("unimplemented")
    }

    @Override
    void submit(content, cb) {
        startTX("submit")
        D.getMap( { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                def String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content.toString()).getBytes()).encodeHex().toString()
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        keyprov.setKey(strAddr, key, {
                            (this.session as kvdnSession).finishTx(this, {
                                eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                cb([result: true, key: key, error: null])
                            })
                        })

                    } else {
                        bailTx([result: false, error: res.cause(), tx: this], cb)
                    }
                })
            } else {
                bailTx([result: false, error: res.cause(), tx: this], cb)
            }
        })
    }

    @Override
    void set(String key, content, cb) {
        startTX("set",[key:key])
        D.getMap( { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        keyprov.setKey(strAddr, key, {
                            (this.session as kvdnSession).finishTx(this, {
                                eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key', key))
                                cb([result: true, key: key, error: null])
                            })
                        })
                    } else {
                        bailTx([result: false, error: res.cause(), tx: this], cb)
                    }
                })
            } else {
                bailTx([result: false, error: res.cause(), tx: this], cb)

            }
        })
    }


    @Override
    void get(String key, cb) {
        startTX("get",[key:key])
        D.getMap( { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                def AsyncMap map = res.result();
                map.get(key, { resGet ->
                    if (resGet.succeeded()) {
                        (this.session as kvdnSession).finishTx(this, {
                            cb([result: resGet.result(), error: null])
                        })
                    } else {
                        bailTx([result: false, error: res.cause(), tx: this], cb)
                    }
                })
            } else {
                bailTx([result: false, error: res.cause(), tx: this], cb)
            }
        })
    }

    @Override
    void del(String key, cb) {
        startTX("del",[key:key])
        D.getMap( { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                map.remove(key, { resDel ->
                    if (resDel.succeeded()) {
                        keyprov.deleteKey(strAddr, key, {
                            (this.session as kvdnSession).finishTx(this, {
                                eb.publish("_KVDN_-${strAddr}", new JsonObject().put('key', key))
                                cb([result: true, key: key, error: null])
                            })
                        })
                    } else {
                        bailTx([result: false, error: res.cause(), tx: this], cb)
                    }
                })
            } else {
                bailTx([result: false, error: res.cause(), tx: this], cb)
            }
        })
    }

    @Override
    void getKeys(cb) {
        startTX("getKeys")
        keyprov.getKeys(this.strAddr, { Map asyncResult ->
            (this.session as kvdnSession).finishTx(this, {
                cb(asyncResult)
            })
        })
    }

    @Override
    void size(cb) {
        startTX("size")
        D.getMap( { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                def AsyncMap map = res.result();
                map.size({ resGet ->
                    if (resGet.succeeded()) {
                        (this.session as kvdnSession).finishTx(this, {
                            cb([result: resGet.result(), error: null])
                        })
                    } else {
                        bailTx([result: false, error: res.cause(), tx: this], cb)
                    }
                })
            } else {
                bailTx([result: false, error: res.cause(), tx: this], cb)
            }
        })
    }

}