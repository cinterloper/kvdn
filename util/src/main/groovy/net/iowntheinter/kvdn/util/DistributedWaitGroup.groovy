package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.def.KvdnSessionInterface

import java.util.function.Function


/**
 * Created by g on 9/24/16.
 */
@TypeChecked
@CompileStatic
//@FixMe need an interface for KvdnSessionInterface available here
class DistributedWaitGroup {
    Vertx vertx
    long timeout
    Handler abortcb, triggercb
    private boolean ran
    private Map events
    EventBus eb
    Logger logger


    /*
    distributed wait group gets its own (temporary?) storage map
    stores data from each ack?
    how would this work?

     */
    DistributedWaitGroup(Set tokens, long timeout = 0, Handler triggercb, Handler abortcb = {}, Vertx v) {
        this.logger = LoggerFactory.getLogger(this.class.getName())
        logger.debug("initalized new DistributedWaitGroup with tokens: $tokens ")
        ran = false
        this.vertx = v
        this.triggercb = triggercb
        eb = vertx.eventBus()
        this.abortcb = abortcb
        this.timeout = timeout
        events = [:]
        tokens.each { token ->
            events[token] = false
        }
    }

    void abort(Object token, Throwable e ){
        abortcb.handle(e)
    }

    void ack(token) {
        events[token] = true
        logger.debug("dwg ack for $token ${events}")
        check(triggercb)
    }


    void onChannel(String channel, Function<String, String> evaluator = { body -> return body }) {
        MessageConsumer c = eb.consumer(channel, { Message message ->
            String body = message.body() as String
            logger.trace("onAck ${channel} ${body}")
            ack(evaluator.apply(body))
        })
        abortTimer(c, abortcb)
    }

    void onKeys(String straddr, KvdnSessionInterface kvdnSession) {
        //s loose typeing hack, should be a kvdnSession
        def c = kvdnSession.onWrite(straddr, { JsonObject body ->
            logger.trace("onKeys ${straddr} ${body}")
            ack(body.getString('key'))
        })
        abortTimer(c, abortcb)
    }

    void check(Handler cb) {
        boolean run = true
        events.each({ k, v ->
            if (v != true)
                run = false
        })
        if (run && !ran) {
            ran = true
            cb.handle(Future.succeededFuture())
        }

    }

    void abortTimer(MessageConsumer c, Handler abortcb) {
        if (this.timeout != 0) {
            vertx.setTimer(this.timeout, {
                logger.debug("abort timed listener on time exceeded ${c.address()}")
                c.unregister({ abortcb.handle(Future.succeededFuture()) })
            })
        }
    }

}
