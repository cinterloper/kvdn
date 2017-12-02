package net.iowntheinter.kvdn.storage.kv.local

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData

/**
 * Created by g on 7/17/16.
 */
class shimAsyncMap implements AsyncMap {
    LocalMap sham
    SharedData sd
    Logger logger

    shimAsyncMap(Vertx vertx, String name) {
        this.sd = vertx.sharedData()
        sham = sd.getLocalMap(name)
        this.logger = LoggerFactory.getLogger(this.class.name)
    }

    @Override
    void get(Object o, Handler handler) {
        Object result = null
        try {
            result = sham.get(o)
        } catch (e) {
            handler.handle(Future.failedFuture(e))
            return
        }
        handler.handle(Future.succeededFuture(result))
    }

    @Override
    void put(Object o, Object o2, Handler handler) {
        logger.trace("PUT KEY: ${o} VALUE:${o2} ")
        try {
            sham.put(o, o2)
        } catch (e) {
            handler.handle(Future.failedFuture(e))
            return
        }
        handler.handle(Future.succeededFuture())

    }
//@fixme put a timer handler to remove after ttl if value is still the same? invalidate if it changes?
    @Override
    void put(Object o, Object o2, long ttl, Handler handler) {
        logger.trace("PUT KEY: ${o} VALUE:${o2} ")
        try {
            sham.put(o, o2)
        } catch (e) {
            handler.handle(Future.failedFuture(e))
            return
        }
        handler.handle(Future.succeededFuture())
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler handler) {
        if (!sham.keySet().contains(o)) {
            try {
                sham.put(o, o2)
            } catch (e) {
                handler.handle(Future.failedFuture(e))
                return
            }
            handler.handle(Future.succeededFuture())
        } else
            handler.handle(Future.failedFuture(false as String))
    }
//@fixme put a timer handler to remove after ttl if value is still the same? invalidate if it changes?

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler handler) {
        if (!sham.keySet().contains(o)) {
            try {
                sham.put(o, o2)
            } catch (e) {
                handler.handle(Future.failedFuture(e))
                return
            }
            handler.handle(Future.succeededFuture())
        } else
            handler.handle(Future.failedFuture(false as String))
    }

    @Override
    void remove(Object o, Handler handler) {
        try {
            sham.remove(o)
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }
        handler.handle(Future.succeededFuture())
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
        try {
            sham.clear()
        } catch (e) {
            handler.handle(Future.failedFuture(e))
            return
        }
        handler.handle(Future.succeededFuture())
    }

    @Override
    void size(Handler handler) {
        Integer result = null
        try {
            result = sham.size()
        } catch (e) {
            handler.handle(Future.failedFuture(e))
            return
        }
        handler.handle(Future.succeededFuture(result))
    }

    @Override
    void keys(Handler handler) {
        Set result = null
        try {
            result = sham.keySet()
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }

        handler.handle(Future.succeededFuture(result))
    }

    @Override
    void values(Handler handler) {
        Set result = null
        try {
            result = sham.values()
        } catch (e) {
            handler.handle(Future.failedFuture(e))
        }

        handler.handle(Future.succeededFuture(result))

    }

    @Override
    void entries(Handler handler) {
        handler.handle(Future.succeededFuture(sham))
    }
}
