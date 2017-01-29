package net.iowntheinter.kvdn.ignite.key

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.ignite.igniteExtension
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import net.iowntheinter.kvdn.storage.kv.kvdata
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteClosure

import javax.cache.Cache.Entry

/**
 * Created by g on 7/17/16.
 */
class igKeyProvider extends igniteExtension implements keyProvider {

    private final Vertx vertx
    def DataImpl
    igKeyProvider(Vertx vertx, kvdata DataImpl){
        this.vertx = vertx
        this.DataImpl = DataImpl
    }
    @Override
    void getKeys(String name, cb) {
        IgniteCache cache = ignite.cache(name)
        try {
            ArrayList keys = new ArrayList()
            //assume major version is 1
            if (_version >= 8) {
                keys = cache.query(new ScanQuery<String, String>(), transformer).getAll()

            } else {
                keySet = ignite.set(name, null)
                keys = keySet.toArray()
            }
            cb([result: keys, error: null])
        } catch (e) {
            cb([result: null, error: e])
        }
    }

    @Override
    void deleteKey(String name, String key, cb) {
        try {
            if (_version >= 8) {
                cb([result: true, error: null])
            } else {
                keySet = ignite.set(name, null)
                keySet.remove(key)
                cb([result: true, error: null])
            }
        } catch (e) {
            cb([result: false, error: e])
        }
    }

    @Override
    void setKey(String name, String key, cb) {
        try {
            if (_version >= 8) {
                cb([result: true, error: null])
            } else {
                keySet = ignite.set(name, null)
                keySet.add(key)
                cb([result: true, error: null])
            }
        } catch (e) {
            cb([result: false, error: e])
        }
    }
    //this dosent work until https://issues.apache.org/jira/browse/IGNITE-2546 make it into a release
    IgniteClosure<Entry<String, String>, String> transformer =
            new IgniteClosure<Entry<String, String>, String>() {
                @Override
                String apply(Entry<String, String> e) {
                    return e.getKey()
                }
            }

    @Override
    void load(Vertx vertx, Object o) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
