package net.iowntheinter.kvdn.storage.transaction.hooks

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import io.vertx.core.logging.Logger
import net.iowntheinter.kvdn.storage.counter.impl.CtrOp

class MapTracker implements TXNHook {
    Logger logger
    KvdnSession session

    MapTracker() {
        logger = LoggerFactory.getLogger(this.class.name)
        straddrCache = new ArrayList()
        logger.info("constructed MapTracker hook")
    }

    void incrementCtr(KvdnOperation tx, KvdnSession session, Handler cb) {
        CtrOp op = session.newOp("_METACTR:${tx.strAddr}", KvdnSession.DATATYPE.CTR) as CtrOp
        op.addAndGet(1.toLong(), new Handler<AsyncResult<Long>>() {
            @Override
            void handle(AsyncResult<Long> event) {
                if (event.failed()) {
                    cb.handle(Future.failedFuture(event.cause()))

                } else {
                    cb.handle(Future.succeededFuture(event.result()))
                }

            }
        })
    }

    @Override
    HookType getType() {
        return null
    }

    @Override
    void call(KvdnOperation tx, KvdnSession session, Handler cb) {

        logger.info("TYPE: ${tx.type}")
        switch (tx.type) {
            case KvdnOperation.TXTYPE.KV_SET:
                incrementCtr(tx, session, cb)
                break
            case KvdnOperation.TXTYPE.KV_SUBMIT:
                incrementCtr(tx, session, cb)
                break
            case KvdnOperation.TXTYPE.KV_DEL:
                incrementCtr(tx, session, cb)
                break
            case KvdnOperation.TXTYPE.KV_KEYS:
                cb.handle(Future.succeededFuture())
                break
            case KvdnOperation.TXTYPE.KV_SIZE:
                cb.handle(Future.succeededFuture())
                break
            case KvdnOperation.TXTYPE.KV_GET:
                cb.handle(Future.succeededFuture())
                break
            default:
                cb.handle(Future.succeededFuture())
                break

        }
    }
}
