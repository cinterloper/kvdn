package net.iowntheinter.kvdn.storage.kv

/**
 * Created by grant on 11/15/15.
 */
interface TXKV {
    Object bailTx(context)
    void snapshot()
    void submit(content,cb)
    void set(String key,content,cb)
    void get(String key,cb)
    void del(String key,cb)
    void getKeys(cb)
}
