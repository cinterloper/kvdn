package net.iowntheinter.kvdn.gremlin.adapters

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

interface JsonObjectSeralizeable {

    JsonObject toJson()

    void toJson(Handler<AsyncResult<JsonObject>> cb)

}
