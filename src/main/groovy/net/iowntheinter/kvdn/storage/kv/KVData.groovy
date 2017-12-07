package net.iowntheinter.kvdn.storage.kv

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.storage.TXNHook

/**
 * Created by g on 10/6/16.
 */
@TypeChecked
@CompileStatic
interface KVData {
    void getMap(String sa, Handler<AsyncResult<AsyncMap>> h)
    LinkedHashSet<TXNHook> getPreHooks()
    LinkedHashSet<TXNHook> getPostHooks()
}
