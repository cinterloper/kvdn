package net.iowntheinter.kvdn.util

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

interface KvdnHttpServiceSync {
    String set(String straddr, String key, String value, Map<String, String> options)

    String submit(String straddr, String value, Map<String, String> options)

    String get(String straddr, String key, Map<String, String> options)
//
//    void size(String straddr, Map<String, String> options, Handler<AsyncResult<Integer>> resultHandler)
//
    String getKeys(String straddr, Map<String, String> options)
//
//    void del(String straddr, String key, Map<String, String> options, Handler<AsyncResult<Map<String, String>>> resultHandler)
//
//    void query(String straddr, Map<String, String> query, Map<String, String> options, Handler<AsyncResult<Map<String, String>>> resultHandler)

}
