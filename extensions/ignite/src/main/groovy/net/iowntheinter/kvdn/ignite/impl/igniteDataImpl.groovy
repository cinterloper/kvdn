package net.iowntheinter.kvdn.ignite.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.ignite.igniteExtension
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 2/20/17.
 */
class igniteDataImpl extends igniteExtension implements kvdata{
    @Override
    void load(Vertx vertx, Object o) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {

    }

    @Override
    LinkedHashSet<txnHook> getPreHooks() {
        return null
    }

    @Override
    LinkedHashSet<txnHook> getPostHooks() {
        return null
    }
}
