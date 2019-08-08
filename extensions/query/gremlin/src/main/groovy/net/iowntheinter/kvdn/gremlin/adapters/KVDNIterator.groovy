package net.iowntheinter.kvdn.gremlin.adapters

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import net.iowntheinter.kvdn.service.KvdnService
import net.iowntheinter.kvdn.util.KvdnBlockingMap

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

@TypeChecked
@CompileStatic
class KVDNIterator implements Iterator {
    final Vertx vertx
    final String straddr
    final KvdnBlockingMap storage

    final AtomicInteger itr = new AtomicInteger()
    final Long steps
    final Object[] keys //@fixme kvdn should support internal iterators and a keystream, instead of getting them all
    //this would require consistant views though.....
    ArrayList<Consumer> consumers = new ArrayList()

    KVDNIterator(KvdnService svc, Vertx vertx, String straddr) {
        this.vertx = vertx
        this.straddr = straddr
        storage = new KvdnBlockingMap(svc, vertx, straddr, [:])
        steps = storage.size()
        keys = storage.keySet().toArray()
    }

    @Override
    boolean hasNext() {
        return (itr.get() < steps)
    }

    @Override
    Object next() {
        Long indx = itr.getAndIncrement()
        Object nxt = storage.get(keys[indx.toInteger()])
        if (!consumers.isEmpty()) {
            consumers.each { c ->
                c.accept(nxt)
            }
        }
        return nxt
    }

    @Override
    void remove() {
        storage.remove(keys[itr - 1 as Integer])
    }

    @Override
    void forEachRemaining(Consumer consumer) {
        consumers.add(consumer)
    }
}
