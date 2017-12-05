package net.iowntheinter.kvdn.storage.kv.data

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.storage.kv.KVData

/**
 * Created by g on 6/10/17.
 */
@TypeChecked
@CompileStatic
class metadataUpdateHook implements TXNHook {

    KvdnTX kvdnTX
    KVData DataImpl
    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W","RW","D"].toSet()
    @Override
    void call(KvdnTX kvdnTX, KvdnSession kvdnSession, Handler cb) {
        this.kvdnTX = kvdnTX
        KvdnTX.TXTYPE optype = this.kvdnTX.type
        cb.handle(Future.succeededFuture())
    }
}