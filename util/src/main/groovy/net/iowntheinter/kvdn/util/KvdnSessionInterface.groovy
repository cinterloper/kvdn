package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject

@CompileStatic
@TypeChecked
interface KvdnSessionInterface {
    MessageConsumer onWrite(String strAddr, Handler<JsonObject> cb)
    MessageConsumer onWrite(String strAddr, String key, Handler<JsonObject> cb)
}
