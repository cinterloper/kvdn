package net.iowntheinter.kvdn.storage

import net.iowntheinter.kvdn.kvdnTX

/**
 * Created by g on 1/9/17.
 */
interface txnHook {
    void call(kvdnTX tx, kvdnSession session, cb)
}
