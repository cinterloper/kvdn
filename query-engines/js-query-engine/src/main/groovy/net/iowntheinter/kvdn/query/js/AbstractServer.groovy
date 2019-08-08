package net.iowntheinter.kvdn.query.js

import io.vertx.core.AsyncResult
import io.vertx.core.Handler


interface AbstractServer {
    void init(Handler<AsyncResult> cb)
}
