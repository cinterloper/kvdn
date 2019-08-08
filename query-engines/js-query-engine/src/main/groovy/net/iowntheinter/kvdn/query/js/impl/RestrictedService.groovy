package net.iowntheinter.kvdn.query.js.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.service.KvdnService

class RestrictedService implements KvdnService {
    final KvdnService svc
    final Set<String> keys, straddrs

    RestrictedService(KvdnService svc, Set<String> keys, Set<String> straddrs, Set<String> Ops) {
        this.svc = svc
        this.keys = keys
        this.straddrs = straddrs
    }

    @Override
    void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        if (this.straddrs.contains(straddrs) && this.keys.contains(key)) {
            svc.set(straddr, key, value, options, resultHandler)
        }

    }

    @Override
    void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        if (this.straddrs.contains(straddrs)) {
            svc.submit(straddr, value, options, resultHandler)
        }

    }

    @Override
    void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler) {
        if (this.straddrs.contains(straddrs) && this.keys.contains(key)) {
            svc.get(straddr, key, options, resultHandler)
        }
    }

    @Override
    void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler) {

    }

    @Override
    void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler) {

    }

    @Override
    void del(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler) {

    }

    @Override
    void query(String straddr, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {

        throw new Exception("cannot meta query")
    }

    @Override
    void clear(String straddr, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler) {


    }
}
