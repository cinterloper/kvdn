package net.iowntheinter.kvdn.storage.lock.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.lock.TXLCK

class LOperation extends KvdnOperation implements TXLCK {

    def D = new _data_impl()

    private class _data_impl {
        SharedData sd
        Vertx vertx
        String name

        void getLock(LOperation txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            this.sd = vertx.sharedData()
            sd.getLock("${name}", cb)
            logger.trace("starting  kvdn lock operation ${name}, ${txid} with vertx.isClustered() == ${vertx.isClustered()}")

        }
    }
    //@todo there must always be a way to lookup the holder of a lock
    //in a clustered enviornment you must be able to 'contact' whoever holds a lock in the case you think it is stale
    //there may need to be some administrative fence/release operation

    LOperation(String sa, UUID txid, KvdnSession session, Vertx vertx) {
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
    void release(Lock l, Handler<AsyncResult> cb) {
        startOperation(TXTYPE.LCK_RELEASE, {

            try {
                assert checkFlags(TXMODE.MODE_WRITE)
                (this.session as KvdnSession).finishOp(this, {
                    l.release()

                    logger.trace("release:${strAddr}")

                    cb(Future.succeededFuture())
                })
            } catch (e) {
                abortOperation(Future.failedFuture(e), this, cb)
            }
        })
    }


    @Override
    void take(Handler<AsyncResult<Lock>> cb) {
        startOperation(TXTYPE.LCK_ACQUIRE, {
            UUID ID = UUID.randomUUID()
            D.getLock(this, { AsyncResult<Lock> res ->
                if (res.succeeded() && checkFlags(TXMODE.MODE_READ)) {
                    Lock l = res.result()

                    logger.trace("acquire:${strAddr}")
                    (this.session as KvdnSession).finishOp(this, {
                        cb([result: l as Lock, error: null])
                    })
                } else {
                    abortOperation(Future.failedFuture(res.cause()), this, cb)
                }


            })
        })
    }


}