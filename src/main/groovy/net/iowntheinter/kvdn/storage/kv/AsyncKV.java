package net.iowntheinter.kvdn.storage.kv;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import net.iowntheinter.kvdn.storage.kv.impl.KvSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Created by g on 2/20/17.
 */
public interface AsyncKV extends AsyncMap {
    void putAll(Map m, Handler<AsyncResult> h);

    void removeAll(List keys, Handler<AsyncResult> h);

    void snapshot(Handler<AsyncResult<KvSnapshot>> h);
}
