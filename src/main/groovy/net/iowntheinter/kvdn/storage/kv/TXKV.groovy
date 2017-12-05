package net.iowntheinter.kvdn.storage.kv

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler

/**
 * Created by grant on 11/15/15.
 */
@CompileStatic
@TypeChecked
interface TXKV {
    void snapshot()
    void submit(String content, Handler cb)
    void set(String key,String content,Handler cb)
    void get(String key,Handler cb)
    void del(String key,Handler cb)
    void getKeys(Handler cb)
    void size(Handler cb)
}
