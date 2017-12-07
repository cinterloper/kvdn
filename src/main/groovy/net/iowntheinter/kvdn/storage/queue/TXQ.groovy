package net.iowntheinter.kvdn.storage.queue

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer

/**
 * Created by g on 6/14/17.
 */
interface TXQ {

    void enqueue(String value, Handler<AsyncResult> cb)
    void dequeue(String value, Handler<AsyncResult> cb)

}
