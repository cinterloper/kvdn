package net.iowntheinter.kvdn.storage.lock.impl

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.lock.TXLCK

class LTx extends kvdnTX implements TXLCK {

    def D = new _data_impl()

    private class _data_impl {
        SharedData sd
        Vertx vertx
        String name

        void getLock(LTx txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            this.sd = vertx.sharedData()
            sd.getLock("${name}", cb)
            logger.trace("starting  kvdn lock operation ${name}, ${txid} with vertx.isClustered() == ${vertx.isClustered()}")

        }
    }

    LTx(String sa, UUID txid, kvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.session = session as kvdnSession
        this.txid = txid
        strAddr = sa
        logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
    }


    @Override
    void release(Lock l, cb) {
        startTX("LCK:release", {

            try {
                assert checkFlags(txmode.MODE_WRITE)
                (this.session as kvdnSession).finishTx(this, {
                    l.release()
                    cb([result: true, error: null])
                })
            } catch (e) {
                bailTx([error: e.getCause(), tx: this], cb)
            }
        })
    }


    @Override
    void get(cb) {
        startTX("LCK:get", {
            D.getLock(this, { res ->
                if (res.succeeded() && checkFlags(txmode.MODE_READ)) {
                    Counter ctr = res.result()
                    ctr.get({ resGet ->
                        if (resGet.succeeded()) {
                            logger.trace("get:${strAddr}")
                            (this.session as kvdnSession).finishTx(this, {
                                cb([result: resGet.result() as Lock, error: null])
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