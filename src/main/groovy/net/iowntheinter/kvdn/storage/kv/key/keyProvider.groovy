package net.iowntheinter.kvdn.storage.kv.key

/**
 * Created by grant on 5/16/16.
 */
interface keyProvider {
    void getKeys(String name, cb)
    void deleteKey(String map, String name, cb)
    void addKey(String map, String name, cb)

}
