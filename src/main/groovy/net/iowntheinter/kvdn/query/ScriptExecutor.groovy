package net.iowntheinter.kvdn.query

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

interface ScriptExecutor {

    void loadScript(String scriptLoc, Handler<AsyncResult> cb)

    void callScript(Map<String, Object> context, Handler<AsyncResult<Map<String, Object>>> cb)


}
