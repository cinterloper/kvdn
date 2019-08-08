package net.iowntheinter.kvdn.query.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.query.QueryProvider
/*
this query engine returns information about the current KVDN server
it should provide:
  - information about (other) currently enabled QueryProviders
  - other KVDN metadata
 */
class Meta implements QueryProvider {
    @Override
    void query(String addr, JsonObject query, Handler<AsyncResult<JsonObject>> cb) {

    }

    @Override
    void load(Vertx vertx, Handler handler) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
