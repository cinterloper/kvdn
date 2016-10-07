package net.iowntheinter.kvdn.storage.kv.data

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.data
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap

/**
 * Created by g on 10/6/16.
 */
class defaultDataImpl implements data {
    SharedData sd
    Vertx vertx
    String sa
    Logger logger

    defaultDataImpl(Vertx v, String sa) {
        this.logger = new LoggerFactory().getLogger("KvTx:" + sa)
        this.vertx = v
        this.sa = sa
        this.sd = vertx.sharedData()
    }

    void getMap(Handler cb) {
        if (vertx.isClustered()) {  //vertx cluster mode
            sd.getClusterWideMap("${sa}", cb)
            logger.trace("starting clustered kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
        } else {                    // vertx local mode
            logger.trace("starting local kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
            cb.handle(Future.succeededFuture(new shimAsyncMap(vertx, sa)))
        }
    }
}