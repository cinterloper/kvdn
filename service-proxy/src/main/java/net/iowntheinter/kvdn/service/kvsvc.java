package net.iowntheinter.kvdn.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import net.iowntheinter.kvdn.service.impl.kvdnService;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by g on 9/15/16.
 */
@ProxyGen
@VertxGen
public interface kvsvc {
    // A couple of factory methods to create an instance and a proxy
    static kvsvc create(Vertx vertx) {
        return new kvdnService(vertx);
    }

    static kvsvc createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(kvdnService.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }

    void set(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void submit(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void get(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void getSize(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void getKeys(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void delete(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

}
