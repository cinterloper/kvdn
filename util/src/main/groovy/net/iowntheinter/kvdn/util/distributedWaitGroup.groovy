package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

/**
 * Created by g on 9/24/16.
 */

//@CompileStatic //@FixMe need an interface for KvdnSession available here
class distributedWaitGroup {
    Vertx vertx
    int timeout
    def abortcb, triggercb
    private boolean ran
    private Map events
    EventBus eb
    Logger logger

    distributedWaitGroup(Set tokens, timeout = 0, triggercb, abortcb = {}, Vertx v) {
        this.logger = LoggerFactory.getLogger(this.class.getName())
        logger.debug("initalized new distributedWaitGroup with tokens: $tokens ")
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

    void ack(token) {
        events[token] = true
        logger.debug("dwg ack for $token ${events}")
        check(triggercb)
    }


    void onChannel(String channel, evaluator = { body -> return body }) {
        MessageConsumer c = eb.consumer(channel, { message ->
            String body = message.body() as String
            logger.trace("onAck ${channel} ${body}")
            ack(evaluator(body))
        })
        abortTimer(c, abortcb)
    }

    void onKeys(String straddr, kvdnSession) {
        //s loose typeing hack, should be a kvdnSession
        def c = kvdnSession.onWrite(straddr, { JsonObject body ->
            logger.trace("onKeys ${straddr} ${body}")
            ack(body.getString('key'))
        })
        abortTimer(c, abortcb)
    }

    void check(cb) {
        boolean run = true
        events.each({ k, v ->
            if (v != true)
                run = false
        })
        if (run && !ran) {
            ran = true
            cb()
        }

    }

    void abortTimer(c, abortcb) {
        if (this.timeout != 0) {
            vertx.setTimer(this.timeout, {
                logger.debug("abort timed listener on time exceeded ${c.address()}")
                (c as MessageConsumer).unregister({ abortcb() })
            })
        }
    }

}
