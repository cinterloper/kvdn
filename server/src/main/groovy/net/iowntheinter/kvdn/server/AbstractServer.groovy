package net.iowntheinter.kvdn.server

import io.vertx.core.AsyncResult
import io.vertx.core.Handler


interface AbstractServer {
    void init(Map<String,Object> config, Handler<AsyncResult> cb)
}
