package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Store
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.mapdb.XodusExtension
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
class XodusPostTXHook extends XodusExtension implements TXNHook {

    XodusDataImpl DataImpl


    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W", "RW", "D"].toSet()

    @Override
    TXNHook.HookType getType() {
        return TXNHook.HookType.PLUGIN_HOOK
    }

    @Override
    void call(KvdnOperation KvdnOperation, KvdnSession session, Handler cb) {

        logger.trace("calling db.commit() hook for xodus")
        this.DataImpl = session.D as XodusDataImpl
        Store store = DataImpl.store
        Environment env = DataImpl.env
//        this.db = (DataImpl).getdb() as DB
//        try {
//            db.commit()
//        } catch (e) {
//            cb.handle(Future.failedFuture(e))
//            return
//        }
        cb.handle(Future.succeededFuture())
    }
}
