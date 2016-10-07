package net.iowntheinter.kvdn.storage.kv

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.AsyncMap

/**
 * Created by g on 10/6/16.
 */
interface data {
    void getMap(Handler<AsyncResult<AsyncMap>> h)
}
