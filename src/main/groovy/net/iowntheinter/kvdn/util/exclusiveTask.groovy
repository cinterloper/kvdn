package net.iowntheinter.kvdn.util

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

/**
 * Created by g on 12/27/16.
 * this class helps to launch a task after acquiring a cluster wide lock on the name of the task
 * An example use of this is connections to remote messaging systems
 */
class exclusiveTask {
    Vertx vertx
    String name
    Logger logger
    def cb

    exclusiveTask(Vertx vertx, name, cb) {
        this.logger = LoggerFactory.getLogger(this.class.name)
        logger.debug("created new exclusiveTask : $name")
        this.name = name
        this.vertx = vertx
        this.cb = cb
    }

    void exec(alreadyLockedCb) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb(lockAttempt.result())
            } else {
                logger.debug("the task lock for $name is held by another member ")
                alreadyLockedCb(lockAttempt.result())
            }
        })
    }
    //you can pass in a closure that takes the retry time, and modifys it
    //example: random, backoff
    void execWithRetry(long retryTime, offsetFunc = { long rt -> return rt }) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb(lockAttempt.result())
            } else {
                logger.trace("the task lock for $name is held by another member, will retry in $retryTime secs")
                vertx.setTimer(retryTime + (long) offsetFunc(retryTime), {
                    execWithRetry(retryTime ,offsetFunc)
                })

            }
        })
    }


}
