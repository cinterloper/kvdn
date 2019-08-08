package net.iowntheinter.kvdn.ignite.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteFuture

import javax.cache.Cache

/**
 * Created by g on 2/20/17.
 */
@TypeChecked
@CompileStatic
class ShimAsyncIgniteMap implements AsyncMap {
    Vertx vertx
    IgniteCache ic
    def iv
    Logger l = LoggerFactory.getLogger(this.class)

    ShimAsyncIgniteMap(Vertx vertx, IgniteCache db) {
        this.vertx = vertx
        this.ic = db
        this.iv = new IgVxAsyncCoupler()
    }

    @Override
    void get(Object o, Handler h) {
        IgniteFuture f = ic.getAsync(o)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void put(Object o, Object o2, Handler h) {
        IgniteFuture f = ic.putAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler h) {
        IgniteFuture f = ic.putAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
        l.warn("TTL IS NOT IMPLEMENTED")
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler h) {
        IgniteFuture f = ic.putIfAbsentAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler h) {
        IgniteFuture f = ic.putIfAbsentAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void remove(Object o, Handler h) {
        IgniteFuture f = ic.removeAsync(o)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler h) {
        IgniteFuture f = ic.removeAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void replace(Object o, Object o2, Handler h) {
        IgniteFuture f = ic.getAndReplaceAsync(o, o2)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler h) {
        IgniteFuture f = ic.replaceAsync(o, oldValue, newValue)
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void clear(Handler h) {
        IgniteFuture f = ic.clearAsync()
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void size(Handler h) {
        IgniteFuture f = ic.sizeAsync()
        (iv as IgVxAsyncCoupler).couple(f, h)
    }

    @Override
    void keys(Handler handler) {
        vertx.executeBlocking(new Handler<Future>() {
            @Override
            void handle(Future future) {
                ArrayList keys = new ArrayList()
                ic.query(new ScanQuery<>(null)).forEach({ Cache.Entry entry -> keys.add(entry.getKey()) })

                future.complete(keys)
            }
        }, handler)


    }

    @Override
    void values(Handler handler) {
        vertx.executeBlocking(new Handler<Future>() {
            @Override
            void handle(Future future) {
                ArrayList values = new ArrayList()
                ic.query(new ScanQuery<>(null)).forEach({ Cache.Entry entry -> values.add(entry.getValue()) })

                future.complete(values)
            }
        }, handler)

    }

    @Override
    void entries(Handler handler) {
        vertx.executeBlocking(new Handler<Future>() {
            @Override
            void handle(Future future) {
                ArrayList entries = new ArrayList()
                ic.query(new ScanQuery<>(null)).forEach({ Cache.Entry entry -> entries.add(entry) })

                future.complete(entries)
            }
        }, handler)
    }
}
