package net.iowntheinter.kvdn.storage.lock.impl

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.lock.TXLCK

class LTx extends KvdnTX implements TXLCK {

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

    LTx(String sa, UUID txid, KvdnSession session, Vertx vertx) {
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
    void release(Lock l, cb) {
        startTX(TXTYPE.LCK_RELEASE, {

            try {
                assert checkFlags(TXMODE.MODE_WRITE)
                (this.session as KvdnSession).finishTx(this, {
                    l.release()
                    logger.trace("release:${strAddr}")

                    cb([result: true, error: null])
                })
            } catch (e) {
                bailTx([error: e.getCause(), tx: this], cb)
            }
        })
    }


    @Override
    void get(cb) {
        startTX(TXTYPE.LKC_ACQUIRE, {
            D.getLock(this, { res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    Lock l = res.result()

                    logger.trace("acquire:${strAddr}")
                    (this.session as KvdnSession).finishTx(this, {
                        cb([result: l as Lock, error: null])
                    })
                } else {
                    bailTx([error: res.cause(), tx: this], cb)
                }


            })
        })
    }


}