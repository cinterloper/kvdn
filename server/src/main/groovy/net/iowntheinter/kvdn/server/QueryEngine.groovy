package net.iowntheinter.kvdn.server

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import net.iowntheinter.kvdn.server.AbstractServer

abstract class QueryEngine implements AbstractServer {


    @Override
    void init(Map<String,Object> config, Handler<AsyncResult> cb) {

    }
}
