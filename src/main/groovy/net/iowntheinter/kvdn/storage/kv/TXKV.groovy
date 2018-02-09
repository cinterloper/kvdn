package net.iowntheinter.kvdn.storage.kv

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler

/**
 * Created by grant on 11/15/15.
 */
@CompileStatic
@TypeChecked
interface TXKV {
    void snapshot()

    void submit(String content, Handler<AsyncResult<String>> cb)

    void set(String key, String content, Handler<AsyncResult<String>> cb)

    void get(String key, Handler<AsyncResult<String>> cb)

    void del(String key, Handler<AsyncResult<String>> cb)

    void getKeys(Handler<AsyncResult<Set<String>>> cb)

    void size(Handler<AsyncResult<Integer>> cb)

    void clear(Handler<AsyncResult> cb)
}
