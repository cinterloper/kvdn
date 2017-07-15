package net.iowntheinter.kvdn.ignite.util

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import org.apache.ignite.IgniteCache
import org.apache.ignite.lang.IgniteFuture

/**
 * Created by g on 2/20/17.
 */
class shimAsyncIgniteMap implements AsyncMap {
    Vertx vertx
    IgniteCache ic
    def iv
    Logger l = LoggerFactory.getLogger(this.class)

    shimAsyncIgniteMap(Vertx vertx, IgniteCache db){
        this.vertx = vertx
        this.ic = db
        this.iv = new IgVxAsyncCoupler()
    }
    @Override
    void get(Object o, Handler h) {
        ic.get(o)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void put(Object o, Object o2, Handler h) {
        ic.put(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler h) {
        ic.put(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
        l.warn("TTL IS NOT IMPLEMENTED")
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler h) {
        ic.putIfAbsent(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler h) {
        ic.putIfAbsent(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void remove(Object o, Handler h) {
        ic.remove(o)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler h) {
        ic.remove(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void replace(Object o, Object o2, Handler h) {
        ic.getAndReplace(o,o2)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler h) {
        ic.replace(o,oldValue,newValue)
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void clear(Handler h) {
        ic.clear()
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }

    @Override
    void size(Handler h) {
        ic.size()
        IgniteFuture f = ic.future()
        (iv as IgVxAsyncCoupler).couple(f,h)
    }
}
