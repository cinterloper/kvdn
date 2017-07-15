package net.iowntheinter.kvdn.storage.kv.data

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook
import net.iowntheinter.kvdn.storage.kv.impl.KvTx


/**
 * Created by g on 6/10/17.
 */
class metadataUpdateHook implements txnHook {

    private tx
    def DataImpl
    Logger logger = LoggerFactory.getLogger(this.class)
    Set TXTYPES = ["W","RW","D"]
    @Override
    void call(kvdnTX kvdnTX, kvdnSession kvdnSession, cb) {
        this.tx = kvdnTX
        def optype = tx.op
    }
}