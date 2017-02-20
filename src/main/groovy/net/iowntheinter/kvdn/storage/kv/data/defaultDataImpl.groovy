package net.iowntheinter.kvdn.storage.kv.data

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.impl.VertxImpl
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 10/6/16.
 */
class defaultDataImpl implements kvdata {
    SharedData sd
    Vertx vertx
    Logger logger

    defaultDataImpl(Vertx v, kvdnSession s) {
        this.vertx = v

        this.logger = new LoggerFactory().getLogger(this.class.getName())
        this.sd = vertx.sharedData()
    }

    @Override
    void getMap(String sa,Handler cb) {
        if (vertx.isClustered()) {  //vertx cluster mode
            sd.getClusterWideMap("${sa}", cb)
            logger.trace("starting clustered kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
        } else {                    // vertx local mode
            logger.trace("starting local kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
            cb.handle(Future.succeededFuture(new shimAsyncMap(vertx, sa)))
        }
    }

    @Override
    LinkedHashSet<txnHook> getPreHooks() {
        return new LinkedHashSet<txnHook>()
    }

    @Override
    LinkedHashSet<txnHook> getPostHooks() {
        return new LinkedHashSet<txnHook>()
    }
}