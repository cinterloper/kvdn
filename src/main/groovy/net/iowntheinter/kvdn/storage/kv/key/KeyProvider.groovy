package net.iowntheinter.kvdn.storage.kv.key

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import net.iowntheinter.kvdn.util.extensionManager.extension


/**
 * Created by grant on 5/16/16.
 */
@TypeChecked
@CompileStatic
interface KeyProvider extends extension {
    void getKeys(String name, Handler<AsyncResult<Set<String>>> cb)
    void deleteKey(String map, String name, Handler<AsyncResult> cb)
    void setKey(String map, String name, Handler<AsyncResult> cb)

}
