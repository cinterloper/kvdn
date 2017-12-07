package net.iowntheinter.kvdn.storage.counter.impl

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.counter.TXCTR
import net.iowntheinter.kvdn.storage.KvdnSession

/**
 * Created by grant on 11/19/15.
 */

class CtrTx extends KvdnTX implements TXCTR {

    def D = new _data_impl()

    private class _data_impl {
        SharedData sd
        Vertx vertx
        String name

        void getCtr(CtrTx txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            this.sd = vertx.sharedData()
            sd.getCounter("${name}", cb)
            logger.trace("starting kvdn counter operation ${name}, ${txid} with vertx.isClustered() == ${vertx.isClustered()}")
        }
    }

    CtrTx(String sa, UUID txid, KvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.session = session as KvdnSession
        this.txid = txid
        strAddr = sa
        logger = new LoggerFactory().getLogger("${this.class.simpleName}" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
    }


    @Override
    void snapshot() {}


    @Override
    void get(cb) {
        startTX(TXTYPE.CTR_GET, {
            D.getCtr(this, { res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    Counter ctr = res.result()
                    ctr.get({ resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("get:${strAddr}")
                            (this.session as KvdnSession).finishTx(this, {
                                cb([result: resGet.result().toString(), error: null])
                            })
                        } else {
                            bailTx([error: res.cause(), tx: this], cb)
                        }
                    })
                } else {
                    bailTx([error: res.cause(), tx: this], cb)
                }
            })
        })
    }

    @Override
    void addAndGet(long value, cb) {
        startTX(TXTYPE.CTR_ADDNGET, {
            D.getCtr(this, { res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.addAndGet(value, { resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("get:${strAddr}")
                            (this.session as KvdnSession).finishTx(this, {
                                if (value > 0)
                                    eb.publish("_KVDN_c+${strAddr}", new JsonObject().put('ctr', strAddr))
                                else
                                    eb.publish("_KVDN_c-${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb([result: resGet.result().toString(), error: null])
                            })
                        } else {
                            bailTx([error: res.cause(), tx: this], cb)
                        }
                    })
                } else {
                    bailTx([error: res.cause(), tx: this], cb)
                }
            })
        })
    }

    @Override
    void getAndAdd(long value, cb) {
        startTX(TXTYPE.CTR_GETNADD, {
            D.getCtr(this, { res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.getAndAdd(value, { resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("get:${strAddr}")
                            (this.session as KvdnSession).finishTx(this, {
                                if (value > 0)
                                    eb.publish("_KVDN_c+${strAddr}", new JsonObject().put('ctr', strAddr))
                                else
                                    eb.publish("_KVDN_c-${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb([result: resGet.result().toString(), error: null])
                            })
                        } else {
                            bailTx([error: res.cause(), tx: this], cb)
                        }
                    })
                } else {
                    bailTx([error: res.cause(), tx: this], cb)
                }
            })
        })
    }

    @Override
    void compareAndSet(long oldv, long newv, cb) {
        startTX(TXTYPE.CTR_COMPSET, {

            D.getCtr(this, { res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_WRITE)) {
                    Counter ctr = res.result()
                    ctr.compareAndSet(oldv, newv, { resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("ctr cas:${strAddr}")
                            (this.session as KvdnSession).finishTx(this, {
                                eb.publish("_KVDN_c=${strAddr}", new JsonObject().put('ctr', strAddr))
                                cb([result: resGet.result().toString(), error: null])
                            })
                        } else {
                            bailTx([error: res.cause(), tx: this], cb)
                        }
                    })
                } else {
                    bailTx([error: res.cause(), tx: this], cb)
                }
            })
        })
    }


}