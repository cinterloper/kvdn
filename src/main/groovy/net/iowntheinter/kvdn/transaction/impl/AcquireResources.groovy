package net.iowntheinter.kvdn.transaction.impl

import io.vertx.core.Handler
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook

class AcquireResources implements TXNHook{


    // set handler to abort if not successful in 300 seconds
    int defaultTimeout = 300
    //should setup handlers to listen for unlock events on any resources it fails to acquire

    int defaultMaxRetries = 5

    /*
      this should attempt to acquire a lock on the resource spaces associated with the
      operation
     */
    /*
     the operation in question must have a
     */
    //if KVDN is operating in transactional mode, this hook always fire on every op
    //to check for locks on the resource for which it is operating
    @Override
    HookType getType() {
        return null
    }

    @Override
    void call(KvdnOperation op, KvdnSession session, Handler cb) {


    }
}
