package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
class mapdbPostTXHook extends mapdbExtension implements txnHook {

    def DataImpl
    DB db
    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W","RW","D"]
    @Override
    void call(kvdnTX kvdnTX, kvdnSession kvdnSession, cb) {
        logger.trace("calling db.commit() hook for mapdb")
        this.DataImpl = kvdnSession.D
        this.db = (DataImpl as mapdbDataImpl).db
        db.commit()
        cb()
    }
}
