package net.iowntheinter.kvdn.mapdb

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.util.extensionManager.extension

/**
 * Created by g on 1/28/17.
 */
abstract class mapdbExtension implements extension {
    @Override
    void load(Vertx vertx, Handler cb) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
