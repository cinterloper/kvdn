package net.iowntheinter.kvdn.ignite.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.ignite.igniteExtension
import net.iowntheinter.kvdn.ignite.util.IgVxAsyncCoupler
import net.iowntheinter.kvdn.ignite.util.shimAsyncIgniteMap
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook
import org.apache.ignite.IgniteCache

/**
 * Created by g on 2/20/17.
 */
class igniteDataImpl extends igniteExtension implements kvdata{
    def iv
    igniteDataImpl(Vertx vertx, kvdnSession s){
        iv  = new IgVxAsyncCoupler()
    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        IgniteCache<String, String> asyncCache = ignite.cache(s).withAsync()
        handler.handle(Future.succeededFuture(new shimAsyncIgniteMap(vertx,asyncCache)))
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
