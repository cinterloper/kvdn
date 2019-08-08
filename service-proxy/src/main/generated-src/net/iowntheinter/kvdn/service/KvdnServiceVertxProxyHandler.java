/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package net.iowntheinter.kvdn.service;

import net.iowntheinter.kvdn.service.KvdnService;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ProxyHandler;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.HelperUtils;

import io.vertx.core.json.JsonArray;
import net.iowntheinter.kvdn.def.KvdnServiceBase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.iowntheinter.kvdn.service.KvdnService;
/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class KvdnServiceVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 
  private final Vertx vertx;
  private final KvdnService service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;

  public KvdnServiceVertxProxyHandler(Vertx vertx, KvdnService service){
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public KvdnServiceVertxProxyHandler(Vertx vertx, KvdnService service, long timeoutInSecond){
    this(vertx, service, true, timeoutInSecond);
  }

  public KvdnServiceVertxProxyHandler(Vertx vertx, KvdnService service, boolean topLevel, long timeoutSeconds) {
      this.vertx = vertx;
      this.service = service;
      this.timeoutSeconds = timeoutSeconds;
      try {
        this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
            new ServiceExceptionMessageCodec());
      } catch (IllegalStateException ex) {}
      if (timeoutSeconds != -1 && !topLevel) {
        long period = timeoutSeconds * 1000 / 2;
        if (period > 10000) {
          period = 10000;
        }
        this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
      } else {
        this.timerID = -1;
      }
      accessed();
    }


  private void checkTimedOut(long id) {
    long now = System.nanoTime();
    if (now - lastAccessed > timeoutSeconds * 1000000000) {
      close();
    }
  }

    @Override
    public void close() {
      if (timerID != -1) {
        vertx.cancelTimer(timerID);
      }
      super.close();
    }

    private void accessed() {
      this.lastAccessed = System.nanoTime();
    }

  public void handle(Message<JsonObject> msg) {
    try{
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) throw new IllegalStateException("action not specified");
      accessed();
      switch (action) {
        case "set": {
          service.set((java.lang.String)json.getValue("straddr"),
                        (java.lang.String)json.getValue("key"),
                        (java.lang.String)json.getValue("value"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "submit": {
          service.submit((java.lang.String)json.getValue("straddr"),
                        (java.lang.String)json.getValue("value"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "get": {
          service.get((java.lang.String)json.getValue("straddr"),
                        (java.lang.String)json.getValue("key"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "size": {
          service.size((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "getKeys": {
          service.getKeys((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "del": {
          service.del((java.lang.String)json.getValue("straddr"),
                        (java.lang.String)json.getValue("key"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "query": {
          service.query((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("query"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "clear": {
          service.clear((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "ctrGet": {
          service.ctrGet((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "addAndGet": {
          service.addAndGet((java.lang.String)json.getValue("straddr"),
                        json.getValue("value") == null ? null : (json.getLong("value").longValue()),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "getAndAdd": {
          service.getAndAdd((java.lang.String)json.getValue("straddr"),
                        json.getValue("value") == null ? null : (json.getLong("value").longValue()),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "ctrCompareAndSet": {
          service.ctrCompareAndSet((java.lang.String)json.getValue("straddr"),
                        json.getValue("oldv") == null ? null : (json.getLong("oldv").longValue()),
                        json.getValue("newv") == null ? null : (json.getLong("newv").longValue()),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "enqueue": {
          service.enqueue((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        (java.lang.String)json.getValue("value"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "dequeue": {
          service.dequeue((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "qPeek": {
          service.qPeek((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        case "qArrayView": {
          service.qArrayView((java.lang.String)json.getValue("straddr"),
                        (io.vertx.core.json.JsonObject)json.getValue("options"),
                        HelperUtils.createHandler(msg));
          break;
        }
        default: throw new IllegalStateException("Invalid action: " + action);
      }
    } catch (Throwable t) {
      msg.reply(new ServiceException(500, t.getMessage()));
      throw t;
    }
  }
}