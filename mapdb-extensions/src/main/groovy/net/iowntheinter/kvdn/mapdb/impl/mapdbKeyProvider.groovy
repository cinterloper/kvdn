package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.Vertx
import net.iowntheinter.kvdn.mapdb.mapdbData
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import org.mapdb.DB

class mapdbKeyProvider extends mapdbExtension implements keyProvider {

    private final Vertx vertx
    private final DB db
    def DataImpl
    mapdbKeyProvider(){
        throw new Exception("YOU MUST PASS THE DATA IMPL TO ${this.class.name} constructor")
    }
    mapdbKeyProvider(Vertx vertx, DataImpl) {
        this.db = (DataImpl as mapdbDataImpl).db
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String s, cb) {
        Map m = db.hashMap(s).createOrOpen()
        cb([result: m.keySet(), error: null])
    }
//this should be handled by the underling map
    @Override
    void deleteKey(String mapName, String key, cb) {
        cb([result: true, error: null])
    }
//this should be handled by the underling map

    @Override
    void setKey(String mapName, String s1, cb) {
        cb([result: true, error: null])
    }
}