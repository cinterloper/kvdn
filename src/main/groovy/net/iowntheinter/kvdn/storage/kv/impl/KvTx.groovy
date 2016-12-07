package net.iowntheinter.kvdn.storage.kv.impl

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kv.TXKV
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.kvdnSession

import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by grant on 11/19/15.
 */

class KvTx extends kvdnTX implements TXKV {
    def D
    boolean finished


    KvTx(String sa, UUID txid, kvdnSession session, Vertx vertx) {
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as kvdnSession
        this.txid = txid
        this.strAddr = sa
        this.logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        this.sd = vertx.sharedData() as SharedData
        this.eb = vertx.eventBus() as EventBus
        this.D = session.D as kvdata

    }

    @Override
    void snapshot() {
        throw new Exception("unimplemented")
    }

    @Override
    void submit(content, cb) {
        startTX("submit")
        D.getMap(this.strAddr, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                AsyncMap map = res.result()
                String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content.toString()).getBytes()).encodeHex().toString()
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
        startTX("set", [key: key])
        D.getMap(this.strAddr, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                AsyncMap map = res.result()
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
        startTX("get", [key: key])
        D.getMap(this.strAddr, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                AsyncMap map = res.result()
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
        startTX("del", [key: key])
        D.getMap(this.strAddr, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                AsyncMap map = res.result()
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
        D.getMap(this.strAddr, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                AsyncMap map = res.result()
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