package net.iowntheinter.kvdn.storage.meta.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.util.DistributedWaitGroup


/**
 * Created by g on 2/20/17.
 */
@CompileStatic
@TypeChecked
class StoreMetaKVHook extends MetadataHook implements TXNHook {

    ArrayList straddrCache

    Logger logger

    StoreMetaKVHook() {
        logger = LoggerFactory.getLogger(this.class.name)
        straddrCache = new ArrayList()
        logger.info("constructed hook")
    }

    String metaKey(KvdnOperation tx) {
        return tx.strAddr + '?' + tx.opKeys[0]//@fixme
        //are composite-keyd operations a real thing? these are txns....
    }

    void storeMeta(KvdnOperation tx, KvdnSession session, Handler cb) {
        /*
        __METADATA_MAP stores maps that have been used, and their declared types
        by default, they are untyped and mutable
        _DATATYPE_MAP stores keys type information

        creating an object involves checking all the necessarys keys are present and of types that match an archatype
        then the map is stamped with an object type, and becomes immutable

         */



        logger.info("running storeMeta for ${tx.type}")
        if (!straddrCache.contains(tx.strAddr)) {
            KvOp mtx = session.newOp('__METADATA_MAP') as KvOp
            KvOp typeUpdate = session.newOp('__DATATYPE_MAP') as KvOp
            DistributedWaitGroup wg = new DistributedWaitGroup(["useMap", "typeMap"] as Set, cb, session.vertx)

            assert tx.valueType != null
            typeUpdate.set(metaKey(tx), tx.valueType.name(),
                    [valueType: KvdnOperation.VALUETYPE.TYPEINFO] as Map<String, Object>,
                    { AsyncResult r ->
                        if (r.succeeded()) {
                            wg.ack("typeMap")
                        } else {
                            wg.abort("typeMstap", r.cause())
                        }
                    })

            mtx.set(tx.strAddr, KvdnOperation.DATATYPE.STRING_MAP.toString(), { AsyncResult ar ->
                if (ar.succeeded()) {
                    straddrCache.add(tx.strAddr)

                    logger.info("set meta data for ${tx.strAddr}")
//                    cb.call(Future.succeededFuture())
                    wg.ack("useMap")
                } else {
                    logger.error("failed to set meta data for ${tx.strAddr}")
                    wg.abort("useMap", ar.cause())

                }
            })


        } else {
            cb.handle(Future.succeededFuture())
        }
    }

    void remove(KvdnOperation tx, KvdnSession session, Handler cb) {
        logger.info("remove")
        KvOp mtx = session.newOp('__METADATA_MAP') as KvOp
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

    void checkIfEmpty(KvdnOperation tx, KvdnSession session, Handler cb) {
        logger.info("checkIfEmpty")
        if (straddrCache.contains(tx.strAddr)) {
            KvOp ktx = session.newOp(tx.strAddr) as KvOp
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
                    logger.error("failed to take size of ${tx.strAddr}: ${ar.cause()}")

                    cb.handle(ar)
                }
            })
        } else {
            cb.handle(Future.succeededFuture())
        }
    }

    @Override
    HookType getType() {
        return HookType.META_HOOK
    }

    @Override
    void call(KvdnOperation tx, KvdnSession session, Handler cb) {
        logger.info("TYPE: ${tx.type}")
        switch (tx.type) {
            case KvdnOperation.TXTYPE.KV_SET:
                storeMeta(tx, session, cb)
                break
            case KvdnOperation.TXTYPE.KV_SUBMIT:
                storeMeta(tx, session, cb)
                break
            case KvdnOperation.TXTYPE.KV_KEYS:
                cb.handle(Future.succeededFuture())
                break
            case KvdnOperation.TXTYPE.KV_DEL:
                checkIfEmpty(tx, session, cb)
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
