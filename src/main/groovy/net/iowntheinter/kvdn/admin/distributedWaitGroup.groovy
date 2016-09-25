package net.iowntheinter.kvdn.admin

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.storage.kv.impl.kvdnSession

import java.util.concurrent.atomic.AtomicLong

/**
 * Created by g on 9/24/16.
 */
class distributedWaitGroup {
    Vertx vertx
    List<String> peerList
    int timeout
    String channel
    def wgcb
    private boolean ran
    private Map events
    EventBus eb

    distributedWaitGroup(Set tokens, Vertx v) {
        ran = false
        this.vertx = v
        eb = vertx.eventBus()
        peerList = config.peerList
        channel = config.channel
        timeout = config.timeout ?: 0
        events = [:]
        tokens.each { token ->
            events[token] = false
        }
        wgcb = cb
    }


    void onAck(String channel, cb) {
        eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            events[body.getString('key')] = true
            check(cb)
        })
    }

    void onAck(String channel, evaluator, cb) {
        eb.consumer(channel, { message ->
            def body = message.body() as JsonObject
            events[body.getString('key')] = evaluator(body)
            check(cb)
        })

    }

    void onKeys(String straddr, cb) {

        def s = new kvdnSession(vertx)
        s.onWrite(straddr, { JsonObject body ->
            events[body.getString('key')] = true
            check(cb)
        })

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

}
