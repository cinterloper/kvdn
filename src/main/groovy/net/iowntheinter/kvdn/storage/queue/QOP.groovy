package net.iowntheinter.kvdn.storage.queue

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.storage.kv.AsyncIterator

/**
 * Created by g on 6/14/17.
 */
interface QOP {

    void enqueue(String value, Handler<AsyncResult<Long>> cb)

    void dequeue(Handler<AsyncResult<String>> cb)

    void peek(Handler<AsyncResult<String>> cb)

    void arrayView(Handler<AsyncResult<JsonObject>> cb)

    void stat(Handler<AsyncResult<JsonObject>> cb)
}
