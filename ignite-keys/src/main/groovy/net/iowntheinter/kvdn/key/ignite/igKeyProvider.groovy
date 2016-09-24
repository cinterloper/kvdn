package net.iowntheinter.kvdn.key.hazelcast

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.IgniteSet
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.lang.IgniteClosure

import javax.cache.Cache.Entry

/**
 * Created by g on 7/17/16.
 */
class igKeyProvider implements keyProvider {

    private IgniteConfiguration cfg;
    private Ignite ignite;
    private IgniteSet keySet = null;
    def _version
    Logger log = LoggerFactory.getLogger(this.class.getName())

    igKeyProvider() {
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        ignite = Ignition.start(cfg);
        _version = ignite.cluster().localNode().version().minor()

    }

    @Override
    void getKeys(String name, cb) {
        log.warn("IGNITE MINOR VER ${_version}")
        IgniteCache cache = ignite.cache(name);

        ArrayList keys = new ArrayList();
        //assume major version is 1
        if (_version >= 8) {
            keys = cache.query(new ScanQuery<String, String>(), transformer).getAll()

        } else {
            keySet = ignite.set(name, null)
            keys = keySet.toArray()
        }
        cb([result: keys, error: null])

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
    void addKey(String name, String key, cb) {
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
                public String apply(Entry<String, String> e) {
                    return e.getKey();
                }
            };


}
