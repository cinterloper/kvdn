package net.iowntheinter.kvdn.util;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class remoteHandler implements Handler {
    final String channel;
    Vertx vertx;
    EventBus eb;
    Logger logger;
    final Class customType;
    MessageCodec m;

    remoteHandler(Vertx vertx, String channel) {
        this(vertx, channel, null, null);
    }

    remoteHandler(Vertx vertx, String channel, MessageCodec m, Class<Object> customType) {
        logger = LoggerFactory.getLogger(this.getClass().getName());
        this.channel = channel;
        this.vertx = vertx;
        eb = vertx.eventBus();
        this.m = m;
        this.customType = customType;
        if (m != null && customType != null)
            eb.registerDefaultCodec(customType, m);
    }

    @Override
    public void handle(Object event) {
        eb.send(channel, event);
    }

}
