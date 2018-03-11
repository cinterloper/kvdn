package net.iowntheinter.kvdn.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject

@TypeChecked
@CompileStatic
class KvdnHooks {
    private final EventBus eb
    private final Vertx vertx

    
    KvdnHooks(Vertx vertx) {
        this.vertx = vertx
        this.eb = vertx.eventBus()
    }

    KvdnHooks onWrite_f(String strAddr, String key = null, Handler<KVDNTxEvent> cb) {
        eb.consumer("_KVDN_+${strAddr}", { Message<JsonObject> message -> //listen for updates on this key
            if ((key == null) || (message.body().getString('key') == key))
                cb.handle(new KVDNTxEvent(message.body()))
        })
        return this
    }

    KvdnHooks onDelete_f(String strAddr, String key = null, Handler<KVDNTxEvent> cb) {
        eb.consumer("_KVDN_-${strAddr}", { Message<JsonObject> message -> //listen for deletes on this keyset
            if ((key == null) || (message.body().getString('key') == key))
                cb.handle(new KVDNTxEvent(message.body()))
        })
        return this
    }


    MessageConsumer onWrite(String strAddr, String key = null, Handler<KVDNTxEvent> cb) {
        return eb.consumer("_KVDN_+${strAddr}", { Message<JsonObject> message -> //listen for updates on this key
            if ((key == null) || (message.body().getString('key') == key))
                cb.handle(new KVDNTxEvent(message.body()))
        })
    }


    MessageConsumer onDelete(String strAddr, String key = null, Handler<KVDNTxEvent> cb) {
        return eb.consumer("_KVDN_-${strAddr}", { Message<JsonObject> message -> //listen for deletes on this key
            if ((key == null) || (message.body().getString('key') == key))
                cb.handle(new KVDNTxEvent(message.body()))
        })
    }
}
