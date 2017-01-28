package net.iowntheinter.kvdn.mapdb.impl

import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import org.mapdb.DB

class mapdbKeyProvider extends mapdbExtension implements keyProvider {

    DB db

    mapdbKeyProvider(DB db) {
        this.db = db
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