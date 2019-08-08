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
import net.iowntheinter.kvdn.mapdb.MapdbExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import org.mapdb.DB
import org.mapdb.DBMaker

//@TypeChecked
//@CompileStatic
class MapdbDataImpl extends MapdbExtension implements KVData {
    JsonObject config
    Vertx vertx
    public DB db;
    String dbpath = null
    Logger logger = LoggerFactory.getLogger(this.class.getName())
    ArrayList<TXNHook> postHooks = new ArrayList<TXNHook>()
    ArrayList<TXNHook> preHooks = new ArrayList<TXNHook>()

    MapdbDataImpl(Vertx vertx, KvdnSession s) {
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
        postHooks.add(new MapdbPostTXHook() as TXNHook)

    }

    @Override
    Object getdb() {
        return db
    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        vertx.executeBlocking({ Future future ->
            future.complete(new ShimAsyncMapDB(vertx, this.db as DB, s))
        }, { AsyncResult asyncResult ->
            handler.handle(asyncResult)
        })
    }

    @Override
    ArrayList<TXNHook> getPreHooks() {
        return this.preHooks
    }

    @Override
    ArrayList<TXNHook> getPostHooks() {
        return this.postHooks
    }

}