package net.iowntheinter.kvdn.query.js.impl

import delight.nashornsandbox.NashornSandbox
import delight.nashornsandbox.NashornSandboxes
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.query.QueryProvider
import net.iowntheinter.kvdn.service.KvdnService
import net.iowntheinter.kvdn.storage.kv.RestrictedKVOperation

class JSQueryProvider implements QueryProvider { //should this extend abstract verticle?

    final NashornSandbox sandbox

    //final RestrictedService kvs

    final KvdnService svc

    final Vertx vertx

    JSQueryProvider(Vertx vertx, KvdnService svc) {
        sandbox = NashornSandboxes.create().allow(RestrictedService)
        this.vertx = vertx
        this.svc = svc
    }

    Set<String> convertJA2S(JsonArray ar) {
        Set<String> s = new HashSet<String>()
        ar.each { ele ->
            s.add(ele.toString())
        }
        return s
    }

    @Override
    void query(String s, JsonObject jsonObject, Handler<AsyncResult<JsonObject>> handler) {

        vertx
        Set<String> allowedKeys = convertJA2S(jsonObject.getJsonArray("KEYS"))
        Set<String> allowedStraddrs = convertJA2S(jsonObject.getJsonArray("STRADDRS"))
        Set<String> allowedOps = convertJA2S(jsonObject.getJsonArray("OPS"))

        //obtain lock on used resources

        vertx.executeBlocking()
        sandbox.inject("kvs", new RestrictedService(svc, allowedKeys, allowedStraddrs, allowedOps))

        sandbox.get("RESULT")

        //release lock on used resources
    }

    @Override
    void load(Vertx vertx, Handler handler) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
