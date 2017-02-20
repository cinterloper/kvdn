package net.iowntheinter.kvdn.storage.kv

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.storage.kv.impl.kvSnapshot

/**
 * Created by g on 2/20/17.
 */
interface asyncKV extends AsyncMap{
    void putAll(Map m, Handler<AsyncResult> h)
    void removeAll(List keys, Handler<AsyncResult> h)
    void snapshot(Handler<AsyncResult<kvSnapshot>> h)
}
