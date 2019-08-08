package net.iowntheinter.kvdn.storage.kv.data;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import net.iowntheinter.kvdn.KvdnOperation;

public interface TypedAsyncMap extends AsyncMap {
    void getTyped(Object object, Handler<AsyncResult<KvdnOperation.VALUETYPE>> handler);
}
