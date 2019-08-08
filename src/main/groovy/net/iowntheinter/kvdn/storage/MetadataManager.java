package net.iowntheinter.kvdn.storage;

import net.iowntheinter.kvdn.storage.meta.MetadataStore;

import java.util.Map;

/**
 * Created by g on 2/20/17.
 */
public interface MetadataManager {
    void setStore(MetadataStore store);

    Map<String, TXNHook> getHooks();
}
