package net.iowntheinter.kvdn.transaction.impl

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Handler
import io.vertx.core.shareddata.Lock

class Transaction<T> {
    // assert upfront all the resources that need to be under our control
    // - are protected by the appropriate type of guards (locks)
    // - (optionally) contain the value we expect
    //for the transaction for be consistant.
    /*
    we acquire a lock on all those resources in the @AcquireResources hook
    that is the 'transaction' identifier ticket

    we then have a time window (timeout) in which we can use this transaction id
    to update data in locked resources





     */

    Map<String, Transaction> dependents = [:]
    Map<String, Lock> resources = [:]
    CompositeFuture changeSet

    void submit(Handler<AsyncResult> h) {

        //recursive descent, all children should complete bottom up, then i should complete
        //


    }

}
