package net.iowntheinter.kvdn.storage.counter

import io.vertx.core.shareddata.impl.AsynchronousLock

/**
 * Created by g on 7/17/16.
 */
interface TXCTR {

    void snapshot()
    void get(cb)
    void addAndGet(long value,cb)
    void getAndAdd(long value,cb)
    void compareAndSet(long oldv, long newv, cb)

}
