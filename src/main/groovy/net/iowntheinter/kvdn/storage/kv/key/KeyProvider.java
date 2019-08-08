package net.iowntheinter.kvdn.storage.kv.key;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.iowntheinter.kvdn.util.extensionManager.Extension;

import java.util.Set;

/**
 * Created by grant on 5/16/16.
 */
public interface KeyProvider extends Extension {
    void getKeys(String map, Handler<AsyncResult<Set<String>>> cb);

    void deleteKey(String map, String key, Handler<AsyncResult> cb);

    void setKey(String map, String key, Handler<AsyncResult> cb);
}
