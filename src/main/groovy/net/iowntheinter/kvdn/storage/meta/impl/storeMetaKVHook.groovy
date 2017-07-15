package net.iowntheinter.kvdn.storage.meta.impl

import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.meta.metadataStore
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 2/20/17.
 */
class storeMetaKVHook implements txnHook {
    @Override
    void call(kvdnTX tx, kvdnSession session, Object cb) {
        (tx.metaData as metadataStore).setMetadata(tx.strAddr, tx.metabuffer, cb)
    }
}
