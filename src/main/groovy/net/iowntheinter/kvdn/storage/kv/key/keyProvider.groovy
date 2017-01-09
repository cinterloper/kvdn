package net.iowntheinter.kvdn.storage.kv.key

import net.iowntheinter.cornerstone.util.extensionManager.extension

/**
 * Created by grant on 5/16/16.
 */
interface keyProvider extends extension {
    void getKeys(String name, cb)
    void deleteKey(String map, String name, cb)
    void setKey(String map, String name, cb)

}
