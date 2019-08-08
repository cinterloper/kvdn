package net.iowntheinter.kvdn.def;

import io.vertx.core.json.JsonObject;

public interface UnderlieingStorageFactory {
    Object create();
    Object create(JsonObject vertxConfig);
}
