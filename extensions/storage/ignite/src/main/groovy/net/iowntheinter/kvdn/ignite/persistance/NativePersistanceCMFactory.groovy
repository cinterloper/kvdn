package net.iowntheinter.kvdn.ignite.persistance

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import net.iowntheinter.kvdn.def.ClusterManagerFactory
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.DataRegionConfiguration
import org.apache.ignite.configuration.DataStorageConfiguration
import org.apache.ignite.configuration.IgniteConfiguration

@TypeChecked
@CompileStatic
class NativePersistanceCMFactory implements ClusterManagerFactory {
    IgniteConfiguration cfg
    Logger logger = LoggerFactory.getLogger(this.class.name)

    NativePersistanceCMFactory() {
        logger.info("Constructed NativePersistanceCMFactory")
    }

    ClusterManager create(JsonObject vertxConfig = null, IgniteConfiguration cfg = null,
                          DataStorageConfiguration storageCfg = null) {
        logger.info("cluster manager being created ")
        if (null == cfg)
            cfg = new IgniteConfiguration()
        if (null == storageCfg)
            storageCfg = new DataStorageConfiguration()
        if (vertxConfig != null) {
            logger.trace(vertxConfig)
//            if (vertxConfig.getBoolean("Persist")) {
//                storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true)
//            }

            if (vertxConfig.containsKey("IGSwapPath")) {
                storageCfg.getDefaultDataRegionConfiguration().setSwapPath(vertxConfig.getString("IGSwapPath"))
            }
            if (vertxConfig.containsKey("storagePath")) {
              //  storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true)
                storageCfg.setStoragePath(vertxConfig.getString("storagePath"))

                logger.trace("setting storagePath ${storageCfg.storagePath}")
            }
            if (vertxConfig.containsKey("WALPath")) {
                storageCfg.setWalPath(vertxConfig.getString("WALPath"))
            }
        }
        DataRegionConfiguration d = new DataRegionConfiguration().setPersistenceEnabled(true).setName("kvdn_persist")
        storageCfg.setDataRegionConfigurations(d)
        cfg.setDataStorageConfiguration(storageCfg)
        logger.trace(cfg.toString())
        logger.info("IGNITE STORAGE PATH $cfg.dataStorageConfiguration.storagePath")


        Ignite ignite = Ignition.getOrStart(cfg)
        ignite.active(true)
// @todo in a distributed setup, when is this appropriate? we may need a custom ignite clustermanager
        return new IgniteClusterManager(ignite)

    }
}
