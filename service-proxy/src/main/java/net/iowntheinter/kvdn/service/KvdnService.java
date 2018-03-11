package net.iowntheinter.kvdn.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl;

/**
 * Created by g on 9/15/16.
 */
@ProxyGen
@VertxGen
public interface KvdnService {
    // A couple of factory methods to create an instance and a proxy
    static KvdnService create(Vertx vertx) {
        return new KvdnServiceImpl(vertx);
    }

    static KvdnService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(KvdnService.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }

    void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler);

    void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler);

    void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler);

    void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler);

    void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler);

    void del(String straddr, String key, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler);

    void query(String straddr, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler);

}
