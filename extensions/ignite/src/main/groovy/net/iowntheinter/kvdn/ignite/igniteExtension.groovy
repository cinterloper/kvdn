package net.iowntheinter.kvdn.ignite

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.util.extensionManager.extension
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteSet
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration

/**
 * Created by g on 1/8/17.
 */
abstract class igniteExtension implements extension {

    protected IgniteConfiguration cfg
    protected Ignite ignite
    protected IgniteSet keySet = null
    def _version
    Logger log = LoggerFactory.getLogger(this.class.getName())
    final Vertx vertx

    igniteExtension() {

        throw new Exception("you must initalize this with the vertx instance")
    }

    igniteExtension(Vertx vertx) {
        this.vertx = vertx
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        ignite = Ignition.start(cfg)
        assert ignite.cluster().localNode().version().major(), 1
        _version = ignite.cluster().localNode().version().minor()
        log.debug("init key provider with IGNITE 1.${_version}")
    }
    @Override
    void load(Vertx vertx, Object o) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
