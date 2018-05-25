package net.iowntheinter.kvdn.storage.meta.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.TXNHook


/**
 * Created by g on 2/20/17.
 */
@CompileStatic
@TypeChecked
class StoreMetaKVHook implements TXNHook {

    ArrayList straddrCache

    Logger logger

    StoreMetaKVHook() {
        logger = LoggerFactory.getLogger(this.class.name)
        straddrCache = new ArrayList()
        logger.info("constructed hook")
    }

    void storeMeta(KvdnTX tx, KvdnSession session, Handler cb) {
        logger.info("running storeMeta for ${tx.type}")
        if (!straddrCache.contains(tx.strAddr)) {
            KvTx mtx = session.newTx('__METADATA_MAP') as KvTx
            mtx.set(tx.strAddr, 1.toString(), { AsyncResult ar ->
                if (ar.succeeded()) {
                    straddrCache.add(tx.strAddr)
                    logger.info("set meta data for ${tx.strAddr}")
                    cb.handle(Future.succeededFuture())
                } else {
                    logger.error("failed to set meta data for ${tx.strAddr}")
                    cb.handle(ar)
                }
            })
        }else{
            cb.handle(Future.succeededFuture())
        }
    }

    void remove(KvdnTX tx, KvdnSession session, Handler cb) {
        logger.info("remove")
        KvTx mtx = session.newTx('__METADATA_MAP') as KvTx
        mtx.del(tx.strAddr, { AsyncResult dar ->
            if (dar.succeeded()) {
                straddrCache.remove(tx.strAddr)
                logger.info("removed metadata for ${tx.strAddr}")
                cb.handle(Future.succeededFuture())
            } else {
                logger.error("failed to remove metadata for ${tx.strAddr}: ${dar.cause()}")
                cb.handle(dar)
            }
        })
    }

    void checkIfEmpty(KvdnTX tx, KvdnSession session, Handler cb) {
        logger.info("checkIfEmpty")
        if (straddrCache.contains(tx.strAddr)) {
            KvTx ktx = session.newTx(tx.strAddr) as KvTx
            ktx.size({ AsyncResult ar ->
                if (ar.succeeded()) {
                    if (ar.result() == 0) {
                        logger.info("${tx.strAddr} appears empty")
                        remove(tx, session, cb)
                    } else {
                        logger.info("${tx.strAddr} does not appear empty")

                        cb.handle(Future.succeededFuture())
                    }
                } else {
                    logger.error("failed to get size of ${tx.strAddr}: ${ar.cause()}")

                    cb.handle(ar)
                }
            })
        }else{
            cb.handle(Future.succeededFuture())
        }
    }

    @Override
    TXNHook.HookType getType() {
        return TXNHook.HookType.META_HOOK
    }

    @Override
    void call(KvdnTX tx, KvdnSession session, Handler cb) {
        logger.info("TYPE: ${tx.type}")
        switch (tx.type) {
            case KvdnTX.TXTYPE.KV_SET:
                storeMeta(tx, session, cb)
                break
            case KvdnTX.TXTYPE.KV_SUBMIT:
                storeMeta(tx, session, cb)
                break
            case KvdnTX.TXTYPE.KV_KEYS:
                cb.handle(Future.succeededFuture())
                break
            case KvdnTX.TXTYPE.KV_DEL:
                checkIfEmpty(tx, session, cb)
                break
            case KvdnTX.TXTYPE.KV_SIZE:
                cb.handle(Future.succeededFuture())
                break
            case KvdnTX.TXTYPE.KV_GET:
                cb.handle(Future.succeededFuture())
                break
            default:
                cb.handle(Future.succeededFuture())
                break

        }
    }
}
