package net.iowntheinter.kvdn.util

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

interface KvdnHttpService {
    void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler)

    void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler)

//    void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler)
//
//    void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler)
//
//    void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler)
//
//    void del(String straddr, String key, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler)
//
//    void query(String straddr, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler)

}
