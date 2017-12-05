package net.iowntheinter.kvdn.storage.kv.key.impl

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider

/**
 * Created by g on 7/14/16.
 * this provides a local (single non clustered) keyset manager for kvdn maps
 * its really a noop impl, because it uses keySet() onthe same localMap that kvdn is using for storage
 * this is not a possibility with the native vertx clustered asyncMap impl
 */
class LocalKeyProvider implements KeyProvider {
    SharedData sd
    Logger logger = LoggerFactory.getLogger(this.class.name)

    LocalKeyProvider(Vertx vertx, D) {
        sd = vertx.sharedData()
    }

    @Override
    void getKeys(String name, Handler cb) {
        logger.trace("Name: $name keyset: ${sd.getLocalMap(name).keySet()} ")
        cb.handle([result: new ArrayList(sd.getLocalMap(name).keySet()), error: null])
    }

    @Override
    void deleteKey(String map, String name, Handler cb) {
        cb.handle([result: true, error: null])
    }

    @Override
    void setKey(String map, String name, Handler cb) {
        cb.handle([result: true, error: null])
    }

    @Override
    void load(Vertx vertx, Handler  cb) {
        cb.handle(Future.succeededFuture())
    }


    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
