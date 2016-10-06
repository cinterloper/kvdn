package net.iowntheinter.kvdn.util

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kvdnSession

/**
 * Created by g on 9/24/16.
 */
class distributedWaitGroup {
    Vertx vertx
    int timeout
    def abortcb
    private boolean ran
    private Map events
    EventBus eb
    Logger logger

    distributedWaitGroup(Set tokens, timeout=0,abortcb={}, Vertx v) {
        ran = false
        this.vertx = v
        eb = vertx.eventBus()
        this.logger = LoggerFactory.getLogger(this.class.getName())
        this.abortcb=abortcb
        this.timeout = timeout
        events = [:]
        tokens.each { token ->
            events[token] = false
        }
    }

    void ack(String token,cb){
        events[token]=true
        check(cb)
    }
    void onAck(String channel, cb) {
        MessageConsumer c = eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            logger.trace("onAck ${channel} ${body}")
            ack(channel,cb)
        })
        abortTimer(c,abortcb)
    }

    void onAck(String channel, evaluator, cb) {
        MessageConsumer c = eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            logger.trace("onAck ${channel} ${body}")
            events[body.getString('key')] = evaluator(body)
            check(cb)
        })
        abortTimer(c,abortcb)
    }

    void onKeys(String straddr, cb) {
        def s = new kvdnSession(vertx)
        s.init({
            def c = s.onWrite(straddr, { JsonObject body ->
                logger.trace("onKeys ${straddr} ${body}")
                events[body.getString('key')] = true
                check(cb)
            })
            abortTimer(c,abortcb)
        },{})

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
    void abortTimer(MessageConsumer c,abortcb){
        if(this.timeout !=0){
            vertx.setTimer(this.timeout,{
                logger.debug("abort timed listener on time exceeded ${c.address()}")
                c.unregister({abortcb()})
            })
        }
    }

}
