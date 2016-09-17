package net.iowntheinter.kvdn.key.hazelcast

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.IgniteCheckedException
import org.apache.ignite.Ignition
import org.apache.ignite.cache.CacheEntry
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cache.query.Query
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.internal.IgnitionEx
import org.apache.ignite.internal.processors.cache.CacheEntryImpl
import org.apache.ignite.internal.processors.cache.IgniteCacheProxy
import org.apache.ignite.internal.util.typedef.F
import org.apache.ignite.lang.IgniteBiPredicate
import org.apache.ignite.lang.IgniteClosure
import org.apache.ignite.lang.IgniteProductVersion

import javax.cache.Cache
import javax.cache.Cache.Entry;

/**
 * Created by g on 7/17/16.
 */
class igKeyProvider implements keyProvider {

    private IgniteConfiguration cfg;
    private Ignite ignite;
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
            cache.query(new ScanQuery<String, String>(), transformer).getAll().each { CacheEntryImpl ent ->
                keys.add(ent.getKey())
            }
        } else {   // performance--; scalability--
            log.warn("IGNITE 1.7 AND BELOW HAS TERRIBLE getKeys() PERFORMANCE, SEE IGNITE-2546")
            cache.query(new ScanQuery<>()).getAll().each { CacheEntryImpl ent ->
                keys.add(ent.getKey())
            } //full scan of all data ):
        }
        cb([result: keys, error: null])

    }

    @Override
    void deleteKey(String name, String key, cb) {
        cb([result: true, error: null])
    }

    @Override
    void addKey(String name, String key, cb) {
        cb([result: true, error: null])
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
