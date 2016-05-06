package net.iowntheinter.kvdn.storage.kv

/**
 * Created by grant on 11/15/15.
 */
interface TSKV {
    Object bailTx(context)
    void snapshot()
    void submit(content,cb)
    void set(key,content,cb)
    void get(key,cb)
    void del(key,cb)
    Object getKeys(cb)
}
