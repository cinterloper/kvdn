package net.iowntheinter.kvdn.storage.kv;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.Set;

/**
 * KVOperation.java
 * this encapsulates the basic async operations that can be preformed on a Key/Value store
 *
 * @author Grant Haywood
 * @since 11/15/15.
 */
public interface KVOperation {
    void snapshot();


    /*
     * This method allows you to submit a value, and take a key automaticly assigned
     * it can optionally be configured with a hashAlgo, and a truncate value
     * you can also provide a valueType in MIME format
     *
     */
    void submit(String content, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void submit(String content, Handler<AsyncResult<String>> cb);

    void set(String key, String content, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void set(String key, String content, Handler<AsyncResult<String>> cb);

    void putIfAbsent(String key, String content, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void putIfAbsent(String key, String content, Handler<AsyncResult<String>> cb);

    void replaceIfPresent(String key, String expectedContent, String content, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void replaceIfPresent(String key, String expectedContent, String content, Handler<AsyncResult<String>> cb);

    void get(String key, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void get(String key, Handler<AsyncResult<String>> cb);

    void del(String key, Map<String, Object> options, Handler<AsyncResult<String>> cb);

    void del(String key, Handler<AsyncResult<String>> cb);

    void getKeys(Map<String, Object> options, Handler<AsyncResult<Set<String>>> cb);

    void getKeys(Handler<AsyncResult<Set<String>>> cb);

    void size(Map<String, Object> options, Handler<AsyncResult<Integer>> cb);

    void size(Handler<AsyncResult<Integer>> cb);

    void clear(Map<String, Object> options, Handler<AsyncResult<Boolean>> cb);

    void clear(Handler<AsyncResult<Boolean>> cb);
}
