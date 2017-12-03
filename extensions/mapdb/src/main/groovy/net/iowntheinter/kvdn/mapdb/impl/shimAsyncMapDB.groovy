package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import org.mapdb.DB

import java.util.concurrent.ConcurrentMap

/**
 * Created by g on 7/17/16.
 */
class shimAsyncMapDB implements AsyncMap {
    final ConcurrentMap sham
    final DB db
    final Vertx vertx
    final String mapName
    Logger logger = LoggerFactory.getLogger(this.class.name)


    shimAsyncMapDB(Vertx vertx, DB db, String name) {
        this.db = db
        this.sham = this.db.hashMap(name).createOrOpen()
        this.vertx = vertx
        this.mapName = name
    }

    @Override
    void get(Object o, Handler handler) {
        logger.trace("GET:" + o)
        vertx.executeBlocking({ future ->
            Object result = null
            try {
                result = sham.get(o)
                future.complete(result)

            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("GET:" + asyncResult.succeeded())

            handler.handle(asyncResult)
        })
    }

    @Override
    void put(Object o, Object o2, Handler handler) {
        logger.trace("PUT:" + o)
        vertx.executeBlocking({ future ->
            try {
                sham.put(o, o2)
                future.complete()
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("PUT:" + asyncResult.succeeded())

            handler.handle(asyncResult)
        })
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler handler) {
        logger.trace("PUT/TTL:${ttl}:" + o)

        //todo: implement mapdb ttl for expiring entries
        vertx.executeBlocking({ future ->
            try {
                sham.put(o, o2)
                future.complete()
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("PUT/TTL:${ttl}:" + asyncResult.succeeded())

            handler.handle(asyncResult)
        })
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler handler) {
        logger.trace("putIfAbsent:" + o)

        vertx.executeBlocking({ future ->

            if (!sham.get(o))
                try {
                    sham.put(o, o2)
                    future.complete()
                } catch (e) {
                    future.fail(e)
                }
            else
                future.fail(false as String)
        }, { asyncResult ->
            logger.trace("putIfAbsent:" + asyncResult.succeeded())

            handler.handle(asyncResult)
        })
    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler handler) {
        logger.trace("putIfAbsent/TTL:${ttl}:" + o)
        vertx.executeBlocking({ future ->
            if (!sham.get(o))
                try {
                    sham.put(o, o2)
                    future.complete()
                } catch (e) {
                    future.fail(e)
                }
            else
                future.fail(false as String)
        }, { asyncResult ->
            logger.trace("putIfAbsent/TTL:${ttl}:" + asyncResult.succeeded())

            handler.handle(asyncResult)
        })
    }

    @Override
    void remove(Object o, Handler handler) {
        logger.trace("remove:${o}")
        vertx.executeBlocking({ future ->
            try {
                sham.remove(o)
                future.complete()
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("remove:${asyncResult.succeeded()}")

            handler.handle(asyncResult)
        })

    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler handler) {
        logger.trace("removeIfPresent:${o}")

        vertx.executeBlocking({ future ->

            if (sham.get(o))
                try {
                    sham.remove(o)
                    future.complete()
                } catch (e) {
                    future.fail(e)
                } else
                future.fail(false as String)
        }, { asyncResult ->
            logger.trace("removeIfPresent:${asyncResult.succeeded()}")

            handler.handle(asyncResult)
        })
    }

    @Override
    void replace(Object o, Object o2, Handler handler) {
        logger.trace("replace:${o}")
        vertx.executeBlocking({ future ->
            try {
                sham.put(o, o2)
                future.complete()
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("replace:${asyncResult.succeeded()}")

            handler.handle(asyncResult)
        })

    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler handler) {
        logger.trace("replaceIfPresent ${o}")
        vertx.executeBlocking({ future ->
            if (sham.get(o) == oldValue)
                try {
                    sham.put(o, newValue)
                    future.complete()
                } catch (e) {
                    future.fail(e)
                } else
                future.fail(false as String)
        }, { asyncResult ->
            logger.trace("replaceIfPresent ${asyncResult.succeeded()}")

            handler.handle(asyncResult)
        })
    }

    @Override
    void clear(Handler handler) {
        logger.trace("CLEAR")
        vertx.executeBlocking({ future ->
            try {
                sham.clear()
                future.complete()
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("clear:${asyncResult.succeeded()}")
            handler.handle(asyncResult)
        })
    }

    @Override
    void size(Handler handler) {
        logger.trace("size")
        Integer result = null
        vertx.executeBlocking({ future ->
            try {
                result = sham.size()
                future.complete(result)
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("size:${asyncResult.succeeded()}")
            handler.handle(asyncResult)
        })
    }

    @Override
    void keys(Handler handler) {
        logger.trace("KEYS ${mapName}")

        Set result
        vertx.executeBlocking({ future ->
            try {
                result = sham.keySet()
                future.complete(result)
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("KEYS ${asyncResult.succeeded()}")
            handler.handle(asyncResult)
        })
    }

    @Override
    void values(Handler handler) {
        logger.trace("VALUES ${mapName}")
        Collection result
        vertx.executeBlocking({ future ->
            try {
                result = sham.values()
                future.complete(result)
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            logger.trace("VALUES ${asyncResult.succeeded()}")
            handler.handle(asyncResult)
        })
    }

    @Override
    void entries(Handler handler) {
        vertx.executeBlocking({ future ->
            future.complete(sham.clone())
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }
}
