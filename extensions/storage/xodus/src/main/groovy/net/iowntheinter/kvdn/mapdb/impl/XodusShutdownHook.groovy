package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
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
class XodusShutdownHook extends XodusExtension implements TXNHook {

    XodusDataImpl DataImpl
    Store store

    @Override
    HookType getType() {
        return HookType.PLUGIN_HOOK
    }

    @Override
    void call(KvdnOperation KvdnOperation, KvdnSession session, Handler cb) {

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