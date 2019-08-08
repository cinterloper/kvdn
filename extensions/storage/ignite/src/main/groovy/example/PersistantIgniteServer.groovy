package example

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.cluster.ClusterManager
import net.iowntheinter.kvdn.ignite.persistance.NativePersistanceCMFactory


class PersistantIgniteServer {
    public static void main(String[] args) {
        if (!System.getenv("DEPLOY_VERTICLE")) {
            System.err.println("Failure you must set DEPLOY_VERTICLE")
            System.exit(-1)
        }

        Map cfg = [storagePath: "./storage",
                   kvdn       : ["data_implementation": "net.iowntheinter.kvdn.ignite.impl.IgniteDataImpl",
                                 "key_provider"       : "net.iowntheinter.kvdn.ignite.key.IgKeyProvider"]
        ]
        JsonObject jcfg = new JsonObject(cfg)
        ClusterManager cm = new NativePersistanceCMFactory().create(jcfg)

        Vertx vertx
        Vertx.clusteredVertx(new VertxOptions().setClusterManager(cm), {
            AsyncResult<Vertx> result ->
                if (result.succeeded()) {
                    vertx = result.result()
                    vertx.deployVerticle(System.getenv("DEPLOY_VERTICLE"), new DeploymentOptions().setConfig(jcfg),
                            { AsyncResult deployResult ->
                                if (deployResult.succeeded())
                                    println("it should be running" + result.result())
                                else {
                                    println("FAILURE IN DEPLOYMENT ")
                                    System.err.println(deployResult.cause())
                                    deployResult.cause().printStackTrace()
                                }
                            })
                }
        })
    }
}