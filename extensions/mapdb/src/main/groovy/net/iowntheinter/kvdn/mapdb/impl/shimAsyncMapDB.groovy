package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData
import org.mapdb.DB
import org.mapdb.DBMaker

import java.util.concurrent.ConcurrentMap

/**
 * Created by g on 7/17/16.
 */
class shimAsyncMapDB implements AsyncMap {
    final ConcurrentMap sham
    final DB db
    final Vertx vertx

    shimAsyncMapDB(Vertx vertx, DB db, String name) {
        this.db = db
        this.sham = this.db.hashMap(name).createOrOpen()
        this.vertx = vertx
    }

    @Override
    void get(Object o, Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.get(o))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void put(Object o, Object o2, Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.put(o, o2))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler handler) {
        //todo: implement mapdb ttl for expiring entries
        vertx.executeBlocking({ future ->
            future.complete(sham.put(o, o2))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler handler) {
        vertx.executeBlocking({ future ->

            if (!sham.get(o))
                future.complete(sham.put(o, o2))
            else
                future.fail(false as String)
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler handler) {
        vertx.executeBlocking({ future ->
            if (!sham.get(o))
                future.complete(sham.put(o, o2))
            else
                future.fail(false as String)
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void remove(Object o, Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.remove(o))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })

    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler handler) {
        vertx.executeBlocking({ future ->

            if (sham.get(o))
                future.complete(sham.remove(o))
            else
                future.fail(false as String)
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void replace(Object o, Object o2, Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.put(o, o2))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })

    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler handler) {
        vertx.executeBlocking({ future ->
            if (sham.get(o) == oldValue)
                future.complete(sham.put(o, newValue))
            else
                future.fail(false as String)
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void clear(Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.clear())
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    void size(Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.size())
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }


}
