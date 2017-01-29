package net.iowntheinter.kvdn.storage.kv

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 10/6/16.
 */
interface kvdata {
    void getMap(String sa, Handler<AsyncResult<AsyncMap>> h)
    LinkedHashSet<txnHook> getPreHooks()
    LinkedHashSet<txnHook> getPostHooks()
}
