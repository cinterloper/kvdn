package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
class mapdbPostTXHook extends mapdbExtension implements TXNHook {

    def DataImpl
    DB db
    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W", "RW", "D"].toSet()

    @Override
    void call(KvdnTX KvdnTX, KvdnSession session, Handler cb) {
        logger.trace("calling db.commit() hook for mapdb")
        this.DataImpl = session.D
        this.db = (DataImpl as mapdbDataImpl).db
        try {
            db.commit()
        } catch (e) {
            cb.handle(Future.failedFuture(e))
            return
        }
        cb.handle(Future.succeededFuture())
    }
}
