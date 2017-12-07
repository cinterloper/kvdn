package net.iowntheinter.kvdn.query.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.query.queryProvider

class batchRequest implements queryProvider {
    EventBus eb
    @Override
    void query(String addr, JsonObject query, Handler<AsyncResult<JsonObject>> cb) {
        String RespChannel = UUID.randomUUID()
        

    }

    @Override
    void load(Vertx vertx, Handler handler) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
