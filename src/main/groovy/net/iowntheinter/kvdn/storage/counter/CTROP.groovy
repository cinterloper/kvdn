package net.iowntheinter.kvdn.storage.counter

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.Lock

/**
 * Created by g on 7/17/16.
 */
interface CTROP {

    void snapshot(Handler<AsyncResult> cb)

    void get(Handler<AsyncResult<Long>> cb)

    void addAndGet(long value, Handler<AsyncResult<Long>> cb)

    void getAndAdd(long value, Handler<AsyncResult<Long>> cb)

    void compareAndSet(long oldv, long newv, Handler<AsyncResult<Boolean>> cb)

}
