package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider
import org.mapdb.DB

@TypeChecked
@CompileStatic
class mapdbKeyProvider extends mapdbExtension implements KeyProvider {

    private final Vertx vertx
    private final DB db
    def DataImpl

    mapdbKeyProvider() {
        throw new Exception("YOU MUST PASS THE DATA IMPL TO ${this.class.name} constructor")
    }

    mapdbKeyProvider(Vertx vertx, KVData DataImpl) {
        this.db = ((mapdbDataImpl) DataImpl).db//untyped, groovy proxy error
        this.vertx = vertx
        this.DataImpl = DataImpl
    }

    @Override
    void getKeys(String s, Handler cb) {
        Map m = db.hashMap(s).createOrOpen()
        cb.handle([result: m.keySet(), error: null])
    }
//this should be handled by the underling map
    @Override
    void deleteKey(String mapName, String key, Handler cb) {
        cb.handle([result: true, error: null])
    }
//this should be handled by the underling map

    @Override
    void setKey(String mapName, String s1, Handler cb) {
        cb.handle([result: true, error: null])
    }
}