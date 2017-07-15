package net.iowntheinter.kvdn.storage.meta

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by g on 2/20/17.
 */
interface storageRouter {
    /*
    the result should contain:
    [
     providerName
     providerPath
    ]
    */

    void route(String path, Handler<AsyncResult<JsonObject>> cb)
}
