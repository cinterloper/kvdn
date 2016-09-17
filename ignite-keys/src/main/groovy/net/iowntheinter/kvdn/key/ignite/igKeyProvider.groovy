package net.iowntheinter.kvdn.key.hazelcast


import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.IgniteCheckedException
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cache.query.Query
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.internal.IgnitionEx
import org.apache.ignite.internal.util.typedef.F
import org.apache.ignite.lang.IgniteBiPredicate
import org.apache.ignite.lang.IgniteClosure

import javax.cache.Cache
import javax.cache.Cache.Entry;

/**
 * Created by g on 7/17/16.
 */
class igKeyProvider implements keyProvider {

    private IgniteConfiguration cfg;
    private Ignite ignite;

    igKeyProvider() {
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        ignite = Ignition.start(cfg);
    }

    @Override
    void getKeys(String name, cb) {
        try {
            IgniteCache cache = ignite.cache(name);


            IgniteClosure<Entry<String, String>, String> transformer =
                    new IgniteClosure<Entry<String, String>, String>() {
                        @Override
                        public String apply(Entry<String, String> e) {
                            return e.getKey();
                        }
                    };

            //List keys = cache.query(new ScanQuery<String, String>(), transformer).getAll()
            //this dosent work untill ignite >1.7
            cb([result: "Unimplemented untill Ignite 1.7 in vertx", error: null])
        } catch (e) {
            cb([result: null, error: e])
        }
    }

    @Override
    void deleteKey(String name, String key, cb) {
        cb([result: true, error: null])
    }

    @Override
    void addKey(String name, String key, cb) {
        cb([result: true, error: null])
    }


}
