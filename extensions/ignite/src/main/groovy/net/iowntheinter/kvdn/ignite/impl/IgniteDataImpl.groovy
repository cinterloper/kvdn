package net.iowntheinter.kvdn.ignite.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.ignite.IgniteExtension
import net.iowntheinter.kvdn.ignite.util.IgVxAsyncCoupler
import net.iowntheinter.kvdn.ignite.util.ShimAsyncIgniteMap
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.storage.kv.KVData

import org.apache.ignite.IgniteCache

/**
 * Created by g on 2/20/17.
 */
@TypeChecked
@CompileStatic
class IgniteDataImpl extends IgniteExtension implements KVData{
    def iv

    IgniteDataImpl(Vertx vertx, KvdnSession s){
        iv  = new IgVxAsyncCoupler()
    }

    @Override
    Object getdb() {
        return null
    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        IgniteCache<String, String> asyncCache = ignite.cache(s).withAsync()
        handler.handle(Future.succeededFuture(new ShimAsyncIgniteMap(vertx,asyncCache) as AsyncMap))
    }

    @Override
    ArrayList<TXNHook> getPreHooks() {
        return null
    }

    @Override
    ArrayList<TXNHook> getPostHooks() {
        return null
    }

    @Override
    void load(Vertx vertx, Handler handler) {

    }
}
