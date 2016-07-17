package net.iowntheinter.kvdn.storage.kv.impl

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider
import net.iowntheinter.kvdn.storage.kv.key.keyProvider


class kvdnSession {
    Vertx vertx
    def eb
    def logger
    def sessionid
    def keyprov

    kvdnSession(Vertx vx) {
        vertx = vx
        sessionid = UUID.randomUUID().toString()
        logger = new LoggerFactory().getLogger("Kvdnsession:${sessionid.toString()}")
        eb = vertx.eventBus();

        if (vertx.isClustered()) {  //vertx cluster mode
         //load cluster key provider
        } else {                    // vertx local mode
            this.keyprov = new LocalKeyProvider(vertx)
        }
    }

    KvTx newTx(String strAddr) {
        return (new KvTx(strAddr, this,  vertx))
    }

    void onWrite(String strAddr, Closure cb) {
        eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this keyset
            cb(message.body())
        })
    }

    void onDelete(String strAddr, Closure cb) {
        eb.consumer("_KVDN_-${strAddr}", { message -> //listen for updates on this keyset
            cb(message.body())
        })
    }
}
