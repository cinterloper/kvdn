package net.iowntheinter.kvdn.storage

import net.iowntheinter.kvdn.storage.meta.MetadataStore

/**
 * Created by g on 2/20/17.
 */
interface MetadataManager {
    void setStore(MetadataStore store)
    Map<String,TXNHook> getHooks()
}
