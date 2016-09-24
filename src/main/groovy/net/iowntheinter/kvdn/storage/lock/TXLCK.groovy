package net.iowntheinter.kvdn.storage.lock

import io.vertx.core.shareddata.Lock

/**
 * Created by g on 7/17/16.
 */
interface TXLCK {
    Object bailTx(context)
    void get(cb)
    void release(Lock l, cb)
}
