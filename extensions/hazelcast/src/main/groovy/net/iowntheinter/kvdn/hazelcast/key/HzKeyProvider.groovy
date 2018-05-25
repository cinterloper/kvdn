package net.iowntheinter.kvdn.hazelcast.key

import com.hazelcast.core.IMap
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.hazelcast.HzExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider


/**
 * Created by g on 7/17/16.
 */
class HzKeyProvider extends HzExtension implements KeyProvider {
    private final Vertx vertx
    def DataImpl

    HzKeyProvider(Vertx vertx, KVData DataImpl) {
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String name, Handler<AsyncResult<Set<String>>> handler) {
        try {
            IMap map = client.getMap(name)
            Future.succeededFuture(map.keySet())
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }
    }

    @Override
    void deleteKey(String name, String key, Handler<AsyncResult> handler) {
        try { //since we are just refering to the same map, which has native keySet(), the key should already be gone
            //IMap map = client.getMap(name);
            //assert !map.keySet().contains(name) //eeeennnnhhhhh this could be very timing-dependent

            handler.handle(Future.succeededFuture())
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e))
        }

    }

    @Override
    void setKey(String s, String s1, Handler<AsyncResult> handler) {
        try { //since we are just refering to the same map, which has native keySet(), the key should already be there
            //IMap map = client.getMap(name);
            //assert map.keySet().contains(key)

            handler.handle(Future.succeededFuture())
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e))
        }
    }


    @Override
    void load(Vertx vertx, Handler handler) {
        handler.handle(Future.succeededFuture())
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

}
