package net.iowntheinter.kvdn.ignite

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.util.extensionManager.Extension
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteSet
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration

/**
 * Created by g on 1/8/17.
 */
@TypeChecked
@CompileStatic
abstract class IgniteExtension implements Extension {

    protected IgniteConfiguration cfg
    protected Ignite ignite
    protected IgniteSet keySet = null
    int _version
    Logger log = LoggerFactory.getLogger(this.class.getName())
    final Vertx vertx

    IgniteExtension() {

        throw new Exception("you must initalize this with the vertx instance")
    }

    IgniteExtension(Vertx vertx) {
        this.vertx = vertx
        cfg = new IgniteConfiguration().setClientMode(true).setLocalHost("localhost")
        ignite = Ignition.start(cfg)
        assert ignite.cluster().localNode().version().major(), 1
        _version = ignite.cluster().localNode().version().minor()
        log.debug("init key provider with IGNITE 1.${_version}")
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
