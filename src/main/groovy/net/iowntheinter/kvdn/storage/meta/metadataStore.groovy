package net.iowntheinter.kvdn.storage.meta

import com.bettercloud.vault.json.JsonObject

/**
 * Created by g on 2/20/17.
 */
interface metadataStore {

    void setAttr(String path, String name, JsonObject data, cb)
    void listAttrs(String path, cb)
    void removeAttr(String path, String name, cb)
}
