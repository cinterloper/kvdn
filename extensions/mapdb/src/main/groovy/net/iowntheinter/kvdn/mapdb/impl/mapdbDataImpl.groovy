package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import org.mapdb.DB
import org.mapdb.DBMaker

@TypeChecked
@CompileStatic
class mapdbDataImpl extends mapdbExtension implements KVData {
    JsonObject config
    Vertx vertx
    public DB db
    String dbpath = null
    Logger logger = LoggerFactory.getLogger(this.class.getName())
    LinkedHashSet<TXNHook> postHooks = new LinkedHashSet<TXNHook>()
    LinkedHashSet<TXNHook> preHooks = new LinkedHashSet<TXNHook>()



    mapdbDataImpl(Vertx vertx, KvdnSession s) {
        this.vertx = vertx
        JsonObject vertxConfig = vertx.getOrCreateContext().config()
        this.config = vertxConfig.getJsonObject('kvdn') ?: new JsonObject()
        dbpath = config.getJsonObject("mapdb")?.getString("dbPath")
        if (dbpath) {

            db = DBMaker.fileDB(dbpath).make()
        } else {
            db = DBMaker.memoryDB().make()
            logger.warn("initalized MapDB as in-memory database")
            logger.warn("THIS IS NOT PERSISTENT")
        }
        postHooks.add(new mapdbPostTXHook() as TXNHook)

    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        vertx.executeBlocking({ Future future ->
            future.complete( new shimAsyncMapDB(vertx, this.db, s))
        }, { AsyncResult asyncResult ->
            handler.handle(asyncResult)
        })
    }
    @Override
    LinkedHashSet<TXNHook> getPreHooks() {
        return this.preHooks
    }

    @Override
    LinkedHashSet<TXNHook> getPostHooks() {
        return this.postHooks
    }

}