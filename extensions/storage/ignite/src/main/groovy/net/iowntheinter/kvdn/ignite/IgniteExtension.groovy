package net.iowntheinter.kvdn.ignite

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.util.extensionManager.Extension
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteSet
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.DataRegionConfiguration
import org.apache.ignite.configuration.DataStorageConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.internal.processors.cache.persistence.DataRegion

/**
 * Created by g on 1/8/17.
 */
@TypeChecked
@CompileStatic
abstract class IgniteExtension implements Extension {

    protected CacheConfiguration defaultCacheConfig
    protected IgniteConfiguration cfg
    protected Ignite ignite
    protected IgniteSet<String> keySet
    int ig_minor_version, ig_major_version
    Logger log = LoggerFactory.getLogger(this.class.getName())
    final Vertx vertx

    boolean testIgniteVersion() {
//        return ig_minor_version >= 8 || ig_major_version >= 2
        return true // we only support 2.3 + now
    }

    IgniteExtension() {

        //throw new Exception("you must initalize this with the vertx instance")
    }


    IgniteExtension(Vertx vertx, KvdnSession s) {
        this.vertx = vertx
        DataRegionConfiguration d = new DataRegionConfiguration().setPersistenceEnabled(true).setName("kvdn_persist")
        DataStorageConfiguration storageCfg = new DataStorageConfiguration()
        storageCfg.setDataRegionConfigurations(d)
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        cfg.setDataStorageConfiguration(storageCfg)
        ignite = Ignition.getOrStart(cfg)

        this.defaultCacheConfig = new CacheConfiguration<>().setDataRegionName("kvdn_persist")
        assert ignite.cluster().localNode().version().major(), 1
        ig_minor_version = ignite.cluster().localNode().version().minor()
        ig_major_version = ignite.cluster().localNode().version().major()

        log.debug("init key provider with IGNITE ${ig_major_version}.${ig_minor_version}")
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

    CacheConfiguration cacheConfigLookup(String cachename){
        CacheConfiguration ccfg = new CacheConfiguration<>().
                setDataRegionName("kvdn_persist").
                setName(cachename)

        return ccfg

    }
}
