package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.mapdb.mapdbData
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.txnHook
import org.mapdb.DB
import org.mapdb.DBMaker

class mapdbDataImpl extends mapdbExtension implements kvdata {
    JsonObject config
    Vertx vertx
    DB db
    String dbpath = null
    Logger logger = LoggerFactory.getLogger(this.class.getName())
    LinkedHashSet<txnHook> postHooks = new LinkedHashSet<txnHook>()
    LinkedHashSet<txnHook> preHooks = new LinkedHashSet<txnHook>()



    mapdbDataImpl(Vertx vertx) {
        this.vertx = vertx
        this.config = vertx.getOrCreateContext().config().getJsonObject('kvdn') ?: new JsonObject()
        dbpath = config.getJsonObject("mapdb")?.getString("dbPath")
        if (dbpath) {
            db = DBMaker.fileDB(dbpath).make()
        } else {
            db = DBMaker.memoryDB().make()
            logger.warn("initalized MapDB as in-memory database")
            logger.warn("THIS IS NOT PERSISTENT")
        }
        postHooks.add(new mapdbPostTXHook())

    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        vertx.executeBlocking({ future ->
            future.complete( new shimAsyncMapDB(vertx, this.db, s))
        }, { asyncResult ->
            handler.handle(asyncResult)
        })
    }
    @Override
    LinkedHashSet<txnHook> getPreHooks() {
        return this.preHooks
    }

    @Override
    LinkedHashSet<txnHook> getPostHooks() {
        return this.postHooks
    }

}