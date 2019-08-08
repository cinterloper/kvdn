package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import me.escoffier.vertx.completablefuture.VertxCompletableFuture
import net.iowntheinter.kvdn.def.KvdnServiceBase

import java.lang.reflect.Field

@CompileStatic
class KvdnBlockingMap<E, T> implements Map<E, T> {
    final KvdnServiceBase svc
    final Vertx vertx

    final String straddr
    final JsonObject empty = new JsonObject()

    KvdnBlockingMap(KvdnServiceBase svc, Vertx vertx, String straddr, Map<String, Class> typeinfo = null) {
        //if we have typinfo, install it in the shadow type map, if T is Object we can have soft key level typeinfo
        print("created blocking map with straddr: " + straddr)
        assert straddr != null
        this.svc = svc
        this.straddr = straddr
        this.vertx = vertx
    }


    @Override
    int size() {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)

        svc.size(this.getStraddr(), empty, { AsyncResult<Integer> ar ->
            if (ar.succeeded()) {
                println("LOG:KBM:size ${ar.result()}")
                fut.complete(ar.result())
            } else {
                println("LOG:KBM:size failed ${ar.cause().toString()}")

                fut.completeExceptionally(ar.cause())
            }
        })

        return (int) fut.get()
    }

    @Override
    boolean isEmpty() {
        return this.size() == 0
    }

    @Override
    boolean containsKey(Object o) {
        return this.keySet().contains(o)
    }

    @Override
    boolean containsValue(Object o) {


        return keySet().contains(o)
    }

    @Override
    T get(Object o) {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)

        svc.get(this.getStraddr(), o.toString(), empty, { AsyncResult<String> ar ->
            if (ar.succeeded()) {
                Object res = ar.result()
                if (res == "null")
                    res = null //@todo this should probably be fixed under the hood

                fut.complete(res)
            } else {
                fut.completeExceptionally(ar.cause())
                throw (ar.cause())

            }
        })
        return fut.get() as T
    }

    @Override
    Object put(Object key, Object value) {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)

        svc.set(this.getStraddr(), key.toString(), value.toString(), empty, { AsyncResult<Integer> ar ->
            if (ar.succeeded())
                fut.complete(ar.result())
            else {
                fut.completeExceptionally(ar.cause())
                throw (ar.cause())

            }
        })
        return fut.get()
    }

    @Override
    T remove(Object o) {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)
        assert o instanceof String // kvdn dont support non-string keys
        svc.del(this.getStraddr(), o as String, empty, { AsyncResult<Integer> ar ->
            if (ar.succeeded())
                fut.complete(ar.result())
            else {
                fut.completeExceptionally(ar.cause())
                throw (ar.cause())

            }
        })
        fut.get()
        return o as T
    }


    void putAll(Map map, Boolean syncronous = false) {

        if (syncronous) { //one at a time (if this is a large map)
            map.each { key, value ->
                this.put(key, value)
            }
        } else {
            //there is probably some middle ground here where we could set a default buffer of N outstanding submissions
            VertxCompletableFuture f = new VertxCompletableFuture(vertx)
            DistributedWaitGroup d = new DistributedWaitGroup(map.keySet(), new Handler() {
                @Override
                void handle(Object event) {
                    f.complete(null)
                }
            }, vertx)

            map.each { key, value ->
                svc.set(this.getStraddr(), key as String, (value.toString()), empty, { AsyncResult<String> r ->
                    if (r.succeeded())
                        d.ack(r.result())
                    else
                        d.abort(key, r.cause())
                })
            } // async submit all values in no particular order

            f.get() //await them all to finish
        }

    }


    @Override
    void clear() {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)

        svc.clear(this.getStraddr(), empty, { AsyncResult<Set<String>> ar ->
            if (ar.succeeded())
                fut.complete(ar.result())
            else
                fut.completeExceptionally(ar.cause())
        })
        fut.get()

    }

    @Override
    Set keySet() {
        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)

        svc.getKeys(this.getStraddr(), empty, { AsyncResult<Set<String>> ar ->
            if (ar.succeeded())
                fut.complete(ar.result())
            else {
                fut.completeExceptionally(ar.cause())
                throw (ar.cause())

            }
        })
        return fut.get().collect().toSet()
    }

    @Override
    Collection values() {  //@todo some providers should have a more optimized version of this
        LinkedHashSet result = new LinkedHashSet()
        this.keySet().each { key ->
            result.add(this.get(key))
        }

        return result
    }

    @Override
    Set<Entry<E, T>> entrySet() {
        LinkedHashSet result = new LinkedHashSet()
        this.keySet().each { key ->
            result.add(new MapEntry(key, this.get(key)))
        }
        return result
    }
}
