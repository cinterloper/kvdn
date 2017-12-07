package net.iowntheinter.kvdn.storage.kv.data

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook

/**
 * Created by g on 10/6/16.
 */
@TypeChecked
@CompileStatic
class defaultDataImpl implements KVData {
    SharedData sd
    Vertx vertx
    Logger logger

    defaultDataImpl(Vertx v, KvdnSession s) {
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
    LinkedHashSet<TXNHook> getPreHooks() {
        return new LinkedHashSet<TXNHook>()
    }

    @Override
    LinkedHashSet<TXNHook> getPostHooks() {
        return new LinkedHashSet<TXNHook>()
    }
}