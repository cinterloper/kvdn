package net.iowntheinter.kvdn.storage.kv.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.key.impl.LocalKeyProvider
import net.iowntheinter.kvdn.storage.kv.key.keyProvider


class kvdnSession {
    Vertx vertx
    def eb, logger, sessionid, keyprov, config

    kvdnSession(Vertx vx) {
        vertx = vx
        config = vertx.getOrCreateContext().config().getJsonObject('kvdn') ?: new JsonObject()
        sessionid = UUID.randomUUID().toString()
        logger = new LoggerFactory().getLogger("Kvdnsession:${sessionid.toString()}")
        eb = vertx.eventBus();
        if (vertx.isClustered()) {  //vertx cluster mode
          String configured_provider = config.getString('key_provider') ?:
                  'net.iowntheinter.kvdn.storage.kv.key.impl.CRDTKeyProvider'
        try{
            this.keyprov = this.class.classLoader.loadClass(configured_provider)?.newInstance() as keyProvider
        }catch(e){
            e.printStackTrace()
            logger.fatal("could not load key provider $configured_provider : ${e.getMessage()}")
            throw e //erm this is pretty fatal
        }
         //load cluster key provider
        } else {                    // vertx local mode
            this.keyprov = new LocalKeyProvider(vertx)
        }
        logger.trace("starting new kvdn session with clustered = ${vertx.isClustered()} keyprovider = ${this.keyprov}")

    }

    KvTx newTx(String strAddr) {
        return (new KvTx(strAddr, this,  vertx))
    }
//fluent?
    def onWrite(String strAddr, Closure cb) {
        eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this keyset
            cb(message.body())
        })
        return this
    }

    def onWrite(String strAddr, String key, Closure cb) {
        eb.consumer("_KVDN_+${strAddr}", { message -> //listen for updates on this key
            if(message.body() == key)
                cb(message.body())
        })
        return this
    }
    def onDelete(String strAddr, Closure cb) {
        eb.consumer("_KVDN_-${strAddr}", { message -> //listen for deletes on this keyset
            cb(message.body())
        })
        return this
    }

    def onDelete(String strAddr, String key, Closure cb) {
        eb.consumer("_KVDN_-${strAddr}", { message -> //listen for deletes on this key
            if(message.body() == key)
                cb(message.body())
        })
        return this
    }
}
