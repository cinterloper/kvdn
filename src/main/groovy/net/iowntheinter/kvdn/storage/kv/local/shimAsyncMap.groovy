package net.iowntheinter.kvdn.storage.kv.local

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData

/**
 * Created by g on 7/17/16.
 */
class shimAsyncMap implements AsyncMap {
    LocalMap sham;
    SharedData sd;

    shimAsyncMap(Vertx vertx, String name) {
        this.sd = vertx.sharedData()
        sham = sd.getLocalMap(name)
    }

    @Override
    void get(Object o, Handler handler) {
        handler.handle(Future.succeededFuture(sham.get(o)))
    }

    @Override
    void put(Object o, Object o2, Handler completionHandler) {
        completionHandler.handle(Future.succeededFuture(sham.put(o, o2)))
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler completionHandler) {
        completionHandler.handle(Future.succeededFuture(sham.put(o, o2)))
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler completionHandler) {
        if (!sham.get(o))
            completionHandler.handle(Future.succeededFuture(sham.put(o, o2)))
        else
            completionHandler.handle(Future.failedFuture(false as String))
    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler completionHandler) {
        if (!sham.get(o))
            completionHandler.handle(Future.succeededFuture(sham.put(o, o2)))
        else
            completionHandler.handle(Future.failedFuture(false as String))
    }

    @Override
    void remove(Object o, Handler handler) {
        handler.handle(Future.succeededFuture(sham.remove(o)))
    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler handler) {
        if (sham.get(o))
            handler.handle(Future.succeededFuture(sham.remove(o)))
        else
            handler.handle(Future.failedFuture(false as String))
    }

    @Override
    void replace(Object o, Object o2, Handler handler) {
        handler.handle(Future.succeededFuture(sham.put(o, o2)))

    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler handler) {
        if (sham.get(o) == oldValue)
            handler.handle(Future.succeededFuture(sham.put(o, newValue)))
        else
            handler.handle(Future.failedFuture(false as String))
    }

    @Override
    void clear(Handler handler) {
        handler.handle(Future.succeededFuture(sham.clear()))
    }

    @Override
    void size(Handler handler) {
        handler.handle(Future.succeededFuture(sham.size()))
    }


}
