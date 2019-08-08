package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.kvdn.mapdb.MapdbExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider
import org.mapdb.DB

@TypeChecked
@CompileStatic
class MapdbKeyProvider extends MapdbExtension implements KeyProvider {

    private final Vertx vertx
    private final DB db
    def DataImpl

    MapdbKeyProvider() {
        throw new Exception("YOU MUST PASS THE DATA IMPL TO ${this.class.name} constructor")
    }

    MapdbKeyProvider(Vertx vertx, KVData DataImpl) {

        this.db = DataImpl.getdb() as DB
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String s, Handler cb) {
        Map m = db.hashMap(s).createOrOpen()
        cb.handle( Future.succeededFuture(m.keySet()))
    }
//this should be handled by the underling map
    @Override
    void deleteKey(String mapName, String key, Handler cb) {
        cb.handle(Future.succeededFuture())
    }
//this should be handled by the underling map

    @Override
    void setKey(String mapName, String s1, Handler cb) {
        cb.handle(Future.succeededFuture())
    }
}