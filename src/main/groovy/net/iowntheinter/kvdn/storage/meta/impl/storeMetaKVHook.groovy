package net.iowntheinter.kvdn.storage.meta.impl

import io.vertx.core.Handler
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.meta.metadataStore
import net.iowntheinter.kvdn.storage.TXNHook


/**
 * Created by g on 2/20/17.
 */
class storeMetaKVHook implements TXNHook {
    @Override
    void call(KvdnTX tx, KvdnSession session, Handler cb) {
        (tx.metaData as metadataStore).setMetadata(tx.strAddr, tx.metabuffer, cb)
    }
}
