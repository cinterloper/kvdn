package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.mapdb.MapdbExtension
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.storage.kv.KVData
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
class MapdbPostTXHook extends MapdbExtension implements TXNHook {

    KVData DataImpl
    DB db
    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W", "RW", "D"].toSet()

    @Override
    TXNHook.HookType getType() {
        return TXNHook.HookType.PLUGIN_HOOK
    }

    @Override
    void call(KvdnTX KvdnTX, KvdnSession session, Handler cb) {
        logger.trace("calling db.commit() hook for mapdb")
        this.DataImpl = session.D
        this.db = (DataImpl).getdb() as DB
        try {
            db.commit()
        } catch (e) {
            cb.handle(Future.failedFuture(e))
            return
        }
        cb.handle(Future.succeededFuture())
    }
}
