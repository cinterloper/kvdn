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
import jetbrains.exodus.bindings.StringBinding
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Environments
import jetbrains.exodus.env.Store
import jetbrains.exodus.env.StoreConfig

import java.util.concurrent.ConcurrentMap

/**
 * Created by g on 7/17/16.
 */
//@CompileStatic
@TypeChecked
class AsyncXodusMap implements AsyncMap {
    final ConcurrentMap sham
    final Store db
    final Environment env
    final Vertx vertx
    final String mapName
    final String dbPath
    final JsonObject appconf
    Logger logger = LoggerFactory.getLogger(this.class.name)


    AsyncXodusMap(Vertx vertx, Store db, String name) {
        this.db = db
        this.sham = null //this.db.hashMap(name).createOrOpen()
        this.vertx = vertx
        this.mapName = name
        this.appconf = this.vertx.getOrCreateContext().config()
        if (appconf.containsKey('xodus')) {
            this.dbPath = appconf.getJsonObject('xodus').getString('dbpath')
        } else {
            this.dbPath = '/tmp/testdb.xo'
        }
        this.env = Environments.newInstance(this.dbPath)
    }


    @Override
    void get(Object o, Handler handler) {
        this.env.executeInTransaction(txn -> {
            final Store store = env.openStore("${this.mapName}", StoreConfig.WITHOUT_DUPLICATES, txn)
            handler.handle(Future.succeededFuture(store.get(txn, StringBinding.stringToEntry(o.toString()))))
        })
    };

    @Override
    void put(Object o, Object o2, Handler completionHandler) {
        this.env.executeInTransaction(txn -> {
            final Store store = env.openStore("${this.mapName}", StoreConfig.WITHOUT_DUPLICATES, txn)
            store.put(txn, StringBinding.stringToEntry(o.toString()), StringBinding.stringToEntry(o2.toString())) //@fixme should use object not string store
            completionHandler.handle(Future.succeededFuture())
        })
    }

    @Override
    void put(Object o, Object o2, long ttl, Handler completionHandler) {
        this.put(o,o2,completionHandler)
    }

    @Override
    void putIfAbsent(Object o, Object o2, Handler completionHandler) {

    }

    @Override
    void putIfAbsent(Object o, Object o2, long ttl, Handler completionHandler) {
        this.putIfAbsent(o,o2,completionHandler)
    }

    @Override
    void remove(Object o, Handler handler) {

    }

    @Override
    void removeIfPresent(Object o, Object o2, Handler handler) {

    }

    @Override
    void replace(Object o, Object o2, Handler handler) {

    }

    @Override
    void replaceIfPresent(Object o, Object oldValue, Object newValue, Handler handler) {

    }

    @Override
    void clear(Handler handler) {

    }

    @Override
    void size(Handler handler) {

    }

    @Override
    void keys(Handler handler) {

    }

    @Override
    void values(Handler handler) {

    }

    @Override
    void entries(Handler handler) {

    }
}
