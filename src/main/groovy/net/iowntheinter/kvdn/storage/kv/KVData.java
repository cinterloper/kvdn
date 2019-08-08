package net.iowntheinter.kvdn.storage.kv;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import net.iowntheinter.kvdn.storage.TXNHook;

import java.util.ArrayList;

/**
 * Created by g on 10/6/16.
 */
public interface KVData {
    Object getdb();

    void getMap(String sa, Handler<AsyncResult<AsyncMap>> h);

    ArrayList<TXNHook> getPreHooks();

    ArrayList<TXNHook> getPostHooks();
}
