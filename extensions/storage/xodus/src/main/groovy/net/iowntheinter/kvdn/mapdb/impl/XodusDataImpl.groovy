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
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Environments
import jetbrains.exodus.env.Store
import jetbrains.exodus.env.StoreConfig
import jetbrains.exodus.env.Transaction
import jetbrains.exodus.env.TransactionalComputable
import net.iowntheinter.kvdn.mapdb.XodusExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import org.jetbrains.annotations.NotNull


@TypeChecked
@CompileStatic
class XodusDataImpl extends XodusExtension implements KVData {
    JsonObject config
    Vertx vertx
    final Environment env
    final Store store
    String dbpath = null
    Logger logger = LoggerFactory.getLogger(this.class.getName())
    ArrayList<TXNHook> postHooks = new ArrayList<TXNHook>()
    ArrayList<TXNHook> preHooks = new ArrayList<TXNHook>()

    XodusDataImpl(Vertx vertx, KvdnSession s) {
        this.vertx = vertx
        JsonObject vertxConfig = vertx.getOrCreateContext().config()
        this.config = vertxConfig.getJsonObject('kvdn') ?: new JsonObject()
        dbpath = config.getJsonObject("mapdb")?.getString("dbPath")


        if (dbpath) {
            env = Environments.newInstance(dbpath)
            store = env.computeInTransaction(new TransactionalComputable<Store>() {
                @Override
                public Store compute(@NotNull final Transaction txn) {
                    return env.openStore("Mail", StoreConfig.WITHOUT_DUPLICATES, txn)
                }
            })
        } else {
            File f = File.createTempFile("xodus", "db")
            env = Environments.newInstance(f)
            store = env.computeInTransaction(new TransactionalComputable<Store>() {
                @Override
                public Store compute(@NotNull final Transaction txn) {
                    return env.openStore("Mail", StoreConfig.WITHOUT_DUPLICATES, txn)
                }
            })
            logger.warn("initalized Xodus WITH A TEMP FILE ${f.path} ${f.name}")
            logger.warn("THIS IS NOT SAFE")
        }
        postHooks.add(new XodusPostTXHook() as TXNHook)

    }

    @Override
    Object getdb() {
        return store
    }

    Object getdbAsync(Handler<AsyncResult<Object>> cb) {

    }

    @Override
    void getMap(String s, Handler<AsyncResult<AsyncMap>> handler) {
        handler.handle(Future.succeededFuture(new AsyncXodusMap(vertx, this.store, s) as AsyncMap))
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