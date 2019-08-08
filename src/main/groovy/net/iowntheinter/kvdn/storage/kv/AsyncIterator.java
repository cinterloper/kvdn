package net.iowntheinter.kvdn.storage.kv;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface AsyncIterator {
    void hasNext(Handler<AsyncResult<Boolean>> cb);

    void next(Handler<AsyncResult<Boolean>> cb);
}
