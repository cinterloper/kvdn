package net.iowntheinter.kvdn.util

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.storage.kv.impl.kvdnSession

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

    distributedWaitGroup(Set tokens, timeout=0,abortcb={}, Vertx v) {
        ran = false
        this.vertx = v
        eb = vertx.eventBus()
        this.abortcb=abortcb
        this.timeout = timeout
        events = [:]
        tokens.each { token ->
            events[token] = false
        }
    }


    void onAck(String channel, cb) {
        MessageConsumer c = eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            events[body.getString('key')] = true
            check(cb)
        })
        abortTimer(c,abortcb)
    }

    void onAck(String channel, evaluator, cb) {
        MessageConsumer c = eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            events[body.getString('key')] = evaluator(body)
            check(cb)
        })
        abortTimer(c,abortcb)
    }

    void onKeys(String straddr, cb) {

        def s = new kvdnSession(vertx)
        def c = s.onWrite(straddr, { JsonObject body ->
            events[body.getString('key')] = true
            check(cb)
        })
        abortTimer(c,abortcb)

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
                c.unregister({abortcb()})
            })
        }
    }

}
