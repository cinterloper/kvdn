package net.iowntheinter.kvdn.def;

import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;

public interface ClusterManagerFactory extends UnderlieingStorageFactory{
    ClusterManager create();
    ClusterManager create(JsonObject vertxConfig);
}
