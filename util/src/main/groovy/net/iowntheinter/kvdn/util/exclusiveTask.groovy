package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.util.function.LongFunction

/**
 * Created by g on 12/27/16.
 * this class helps to launch a task after acquiring a cluster wide lock on the name of the task
 * An example use of this is connections to remote messaging systems
 */
@TypeChecked
@CompileStatic
class exclusiveTask {
    Vertx vertx
    String name
    Logger logger
    Handler cb

    exclusiveTask(Vertx vertx, String name, Handler cb) {
        this.logger = LoggerFactory.getLogger(this.class.name)
        logger.debug("created new exclusiveTask : $name")
        this.name = name
        this.vertx = vertx
        this.cb = cb
    }

    void exec(Handler alreadyLockedCb) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb.handle(lockAttempt.result())
            } else {
                logger.debug("the task lock for $name is held by another member ")
                alreadyLockedCb.handle(lockAttempt.result())
            }
        })
    }
    //you can pass in a closure that takes the retry time, and modifys it
    //example: random, backoff
    void execWithRetry(long retryTime, LongFunction<Long> offsetFunc = { long rt -> return rt }) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb.handle(lockAttempt.result())
            } else {
                logger.trace("the task lock for $name is held by another member, will retry in $retryTime secs")
                vertx.setTimer(retryTime + (long) offsetFunc.apply(retryTime ), {
                    execWithRetry(retryTime, offsetFunc)
                })

            }
        })
    }


}
