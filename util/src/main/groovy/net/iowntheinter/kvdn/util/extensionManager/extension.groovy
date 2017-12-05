package net.iowntheinter.kvdn.util.extensionManager

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

/**
 * Created by g on 1/8/17.
 */
interface extension {
    void load(Vertx v, Handler cb)

    JsonObject register(Vertx v)
}
