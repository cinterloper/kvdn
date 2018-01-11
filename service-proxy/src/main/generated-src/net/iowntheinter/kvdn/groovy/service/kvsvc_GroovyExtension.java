package net.iowntheinter.kvdn.groovy.service;
public class kvsvc_GroovyExtension {
  public static void set(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.lang.String key, java.lang.String value, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.set(straddr,
      key,
      value,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.core.impl.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void submit(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.lang.String value, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.submit(straddr,
      value,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.core.impl.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void get(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.lang.String key, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>> resultHandler) {
    j_receiver.get(straddr,
      key,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.String>>() {
      public void handle(io.vertx.core.AsyncResult<java.lang.String> ar) {
        resultHandler.handle(ar.map(event -> event));
      }
    } : null);
  }
  public static void size(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Integer>> resultHandler) {
    j_receiver.size(straddr,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Integer>>() {
      public void handle(io.vertx.core.AsyncResult<java.lang.Integer> ar) {
        resultHandler.handle(ar.map(event -> event));
      }
    } : null);
  }
  public static void getKeys(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.List<Object>>> resultHandler) {
    j_receiver.getKeys(straddr,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonArray> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.core.impl.ConversionHelper.fromJsonArray(event)));
      }
    } : null);
  }
  public static void delete(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.lang.String key, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.delete(straddr,
      key,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.core.impl.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
  public static void query(net.iowntheinter.kvdn.service.kvsvc j_receiver, java.lang.String straddr, java.util.Map<String, Object> query, java.util.Map<String, Object> options, io.vertx.core.Handler<io.vertx.core.AsyncResult<java.util.Map<String, Object>>> resultHandler) {
    j_receiver.query(straddr,
      query != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(query) : null,
      options != null ? io.vertx.core.impl.ConversionHelper.toJsonObject(options) : null,
      resultHandler != null ? new io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject> ar) {
        resultHandler.handle(ar.map(event -> io.vertx.core.impl.ConversionHelper.fromJsonObject(event)));
      }
    } : null);
  }
}
