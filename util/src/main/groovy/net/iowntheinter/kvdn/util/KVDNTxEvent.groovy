package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.json.JsonObject

@TypeChecked
@CompileStatic
class KVDNTxEvent {
    public final JsonObject raw_event
    public final String key
    public final boolean failure

    KVDNTxEvent(JsonObject raw_event) {
        this.raw_event = raw_event
        if (raw_event.getBoolean('failure')) {
            failure = true
            key = null
        } else {
            failure = false
            key = raw_event.getString('key')
        }
    }
}
