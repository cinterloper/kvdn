package net.iowntheinter.kvdn.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientNetworkConfig
import com.hazelcast.core.HazelcastInstance
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.cornerstone.util.extensionManager.extension


/**
 * Created by g on 1/8/17.
 */
abstract class hzExtension implements extension{
    protected HazelcastInstance client
    final protected Vertx vertx
    Logger logger
    hzExtension(){ //this is a farce
        throw new Exception("extensions must be initialized with the vertx object")
    }
    hzExtension(Vertx vertx) {
        this.vertx = vertx
        this.logger = LoggerFactory.getLogger(this.class.getName())
        ClientNetworkConfig cnc = new ClientNetworkConfig().addAddress("127.0.0.1:5701");
        ClientConfig clientConfig = new ClientConfig().setNetworkConfig(cnc)
        client = HazelcastClient.newHazelcastClient(clientConfig)
    }
}
