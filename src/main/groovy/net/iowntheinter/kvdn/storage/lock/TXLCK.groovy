package net.iowntheinter.kvdn.storage.lock

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.Lock

/**
 * Created by g on 7/17/16.
 */
interface TXLCK {
    void take(Handler<AsyncResult<Lock>> cb)
    void release(Lock l, Handler<AsyncResult> cb)
}
