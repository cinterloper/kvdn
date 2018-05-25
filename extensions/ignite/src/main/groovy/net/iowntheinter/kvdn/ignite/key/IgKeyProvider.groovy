package net.iowntheinter.kvdn.ignite.key

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.ignite.IgniteExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteClosure

import javax.cache.Cache.Entry

/**
 * Created by g on 7/17/16.
 */
@TypeChecked
@CompileStatic
class IgKeyProvider extends IgniteExtension implements KeyProvider {

    private final Vertx vertx
    def DataImpl

    IgKeyProvider(Vertx vertx, KVData DataImpl) {
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String name, Handler<AsyncResult<Set<String>>> handler) {
        IgniteCache cache = ignite.cache(name)
        try {
            ArrayList<String> keys
            //assume major version is 1
            if (_version >= 8) {
                keys = new ArrayList<String>(cache.query(new ScanQuery<String, String>(), transformer).getAll())

            } else {
                keySet = ignite.set(name, null)
                keys = new ArrayList(keySet)
            }
            handler.handle(Future.succeededFuture(keys.toSet()))
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }
    }

    @Override
    void deleteKey(String map, String key, Handler<AsyncResult> handler) {
        try {
            if (_version >= 8) { // we dont need to keep track of a key set
                handler.handle(Future.succeededFuture())
            } else {
                keySet = ignite.set(map, null)
                keySet.remove(key)
                handler.handle(Future.succeededFuture())
            }
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }
    }

    @Override
    void setKey(String map, String key, Handler<AsyncResult> handler) {
        try {
            if (_version >= 8) {
                handler.handle(Future.succeededFuture())
            } else {
                keySet = ignite.set(map, null)
                keySet.add(key)
                handler.handle(Future.succeededFuture())
            }
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }
    }
    //this dosent work until https://issues.apache.org/jira/browse/IGNITE-2546 make it into a release
    IgniteClosure<Entry<String, String>, String> transformer =
            new IgniteClosure<Entry<String, String>, String>() {
                @Override
                String apply(Entry<String, String> e) {
                    return e.getKey()
                }
            }


    @Override
    void load(Vertx vertx, Handler handler) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

}
