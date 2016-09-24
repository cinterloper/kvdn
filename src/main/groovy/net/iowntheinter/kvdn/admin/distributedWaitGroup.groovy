package net.iowntheinter.kvdn.admin

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

import java.util.concurrent.atomic.AtomicLong

/**
 * Created by g on 9/24/16.
 */
class distributedWaitGroup {
    List<String> peerList
    int timeout
    def wgcb

    distributedWaitGroup(Map config, Vertx v, cb) {
        peerList = config.peerList ?: []
        timeout = config.timeout ?: 0
        wgcb = cb
    }


    void onAck(JsonObject rpc, result = true) {

    }

    void onKeys(Set keys) {

    }
}
