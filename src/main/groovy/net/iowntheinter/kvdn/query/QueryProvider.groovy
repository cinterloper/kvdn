package net.iowntheinter.kvdn.query

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.util.extensionManager.Extension

/**
 * Created by g on 1/8/17.
 */
@TypeChecked
@CompileStatic
interface QueryProvider extends Extension {
    void query(String addr, JsonObject query, Handler<AsyncResult<JsonObject>> cb)
}
