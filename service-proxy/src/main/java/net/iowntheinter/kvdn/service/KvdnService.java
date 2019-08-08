package net.iowntheinter.kvdn.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import net.iowntheinter.kvdn.def.KvdnServiceBase;
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl;
import net.iowntheinter.kvdn.storage.kv.AsyncIterator;

/**
 * Created by g on 9/15/16.
 */
@ProxyGen
@VertxGen
public interface KvdnService extends KvdnServiceBase {
    // A couple of factory methods to create an instance and a proxy
    static KvdnService create(Vertx vertx) {
        return new KvdnServiceImpl(vertx);
    }

    static KvdnService createProxy(Vertx vertx, String address) {
        return new ServiceProxyBuilder(vertx).setAddress(address).build(KvdnService.class);
        //return ProxyHelper.createProxy(KvdnService.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }

    void set(String straddr, String key, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler);

    void submit(String straddr, String value, JsonObject options, Handler<AsyncResult<String>> resultHandler);

    void get(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler);

    void size(String straddr, JsonObject options, Handler<AsyncResult<Integer>> resultHandler);

    void getKeys(String straddr, JsonObject options, Handler<AsyncResult<JsonArray>> resultHandler);

    void del(String straddr, String key, JsonObject options, Handler<AsyncResult<String>> resultHandler);

    void query(String straddr, JsonObject query, JsonObject options, Handler<AsyncResult<JsonObject>> resultHandler);

    void clear(String straddr, JsonObject options, Handler<AsyncResult<Boolean>> resultHandler);

    void ctrGet(String straddr, JsonObject options, Handler<AsyncResult<Long>> cb);

    void addAndGet(String straddr, long value, JsonObject options, Handler<AsyncResult<Long>> cb);

    void getAndAdd(String straddr, long value, JsonObject options, Handler<AsyncResult<Long>> cb);

    void ctrCompareAndSet(String straddr, long oldv, long newv, JsonObject options, Handler<AsyncResult<Boolean>> cb);

    void enqueue(String straddr, JsonObject options, String value, Handler<AsyncResult<Long>> cb);

    void dequeue(String straddr, JsonObject options, Handler<AsyncResult<String>> cb);

    void qPeek(String straddr, JsonObject options, Handler<AsyncResult<String>> cb);

    void qArrayView(String straddr, JsonObject options, Handler<AsyncResult<JsonObject>> cb);

}
