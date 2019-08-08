package net.iowntheinter.kvdn.ignite.key

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.ignite.IgniteExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider

import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.configuration.CollectionConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
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

    Logger logger = LoggerFactory.getLogger(this.class.name)

    IgKeyProvider(Vertx vertx, KVData DataImpl) {
        //@todo should generalize this so it really can use @IgniteExtension 's  constructor
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        ignite = Ignition.getOrStart(cfg)
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String name, Handler<AsyncResult<Set<String>>> handler) {
//        IgniteCache<String, Object> cache = ignite.cache(name)
//
//        if(cache == null){
//            logger.error("cache $name does not exist yet")
//        }
        IgniteCache<String, Object> cache = ignite.getOrCreateCache(name)
        logger.trace("ignite cache names: ${ignite.cacheNames()}")

        try {
            ArrayList keys = new ArrayList()
            //assume major version is 1
            if (testIgniteVersion()) {
                logger.debug(" ig version minor major $ig_minor_version >= 8 || $ig_major_version >= 2, using transoformer for getKeys")

                cache.query(new ScanQuery<>(null)).forEach({ Entry entry -> keys.add(entry.getKey()) })

               // keys = new ArrayList(cache.query(new ScanQuery<String, Object>(), transformer).getAll())

                // ^ for some reason doing it this way trys to seralized the deployment manager (vertx internal thing?)

            } else {
                logger.trace("getting ignite set for keys $name")
                keySet = ignite.set(name, new CollectionConfiguration())
                keys = new ArrayList(keySet)
            }
            handler.handle(Future.succeededFuture(keys.toSet()))
        } catch (e) {
            logger.error(e)
            handler.handle(Future.failedFuture(e))
        }
    }

    @Override
    void deleteKey(String map, String key, Handler<AsyncResult> handler) {
        try {
            if (testIgniteVersion()) { // we dont need to keep track of a key set
                logger.debug(" ig version minor major $ig_minor_version >= 8 || $ig_major_version >= 2, doing nothing in keyprov $ig_minor_version")
                handler.handle(Future.succeededFuture())
            } else {
                logger.debug("removing $key from ignite set $map")
                keySet = ignite.set(map, null)
                keySet.remove(key)
                handler.handle(Future.succeededFuture())
            }
        } catch (e) {
            logger.error(e)
            handler.handle(Future.failedFuture(e))
        }
    }

    @Override
    void setKey(String map, String key, Handler<AsyncResult> handler) {
        try {
            if (testIgniteVersion()) {
                logger.debug(" ig version minor major $ig_minor_version >= 8 || $ig_major_version >= 2, doing nothing in keyprov $ig_minor_version")
                handler.handle(Future.succeededFuture())
            } else {
                logger.debug("adding $key to ignite set $map")
                keySet = ignite.set(map, new CollectionConfiguration())
                logger.trace(keySet.properties)
                logger.trace(keySet.empty)
                keySet.add(key)
                handler.handle(Future.succeededFuture())
            }
        } catch (e) {
            logger.error(e)
            handler.handle(Future.failedFuture(e))
        }
    }
    //this dosent work until https://issues.apache.org/jira/browse/IGNITE-2546 make it into a release
    IgniteClosure<Entry<String, Object>, String> transformer =
            new IgniteClosure<Entry<String, Object>, String>() {
                @Override
                String apply(Entry<String, Object> e) {
                    logger.trace("applying scanquery to ${e.key}")
                    return e.getKey()
                }
            }


    @Override
    void load(Vertx vertx, Handler handler) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return new JsonObject()
    }

}
