package net.iowntheinter.kvdn.storage

import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.meta.metadataStore

/**
 * Created by g on 2/20/17.
 */
interface metadataManager {
    void setStore(metadataStore store)
    Map<String,txnHook> getHooks()
}
