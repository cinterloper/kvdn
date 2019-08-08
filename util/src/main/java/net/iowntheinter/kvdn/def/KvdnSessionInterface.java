package net.iowntheinter.kvdn.def;


import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;


public interface KvdnSessionInterface {

    MessageConsumer onWrite(String strAddr, Handler<JsonObject> cb);
    MessageConsumer onWrite(String strAddr, String key, Handler<JsonObject> cb);
}
