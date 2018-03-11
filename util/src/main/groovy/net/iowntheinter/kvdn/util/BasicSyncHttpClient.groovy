package net.iowntheinter.kvdn.util

import com.mashape.unirest.http.Unirest
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient

@CompileStatic
@TypeChecked
class BasicSyncHttpClient implements KvdnHttpServiceSync {
    String kvdnBaseUrl
    URL url
    Vertx vertx
    WebClient client

    BasicSyncHttpClient(String kvdnBaseUrl, Vertx v) {
        this.kvdnBaseUrl = kvdnBaseUrl
        this.url = new URL(kvdnBaseUrl)
        url.openConnection()
        this.vertx = v
        this.client = WebClient.create(vertx)
    }

    BasicSyncHttpClient(String kvdnBaseUrl) {
        this(kvdnBaseUrl, Vertx.vertx())
    }

    String getKvdnVersion() {

    }

    void request() {

    }

    @Override
    String set(String straddr, String key, String value, Map<String, String> headers = [:]) {
        return Unirest.put("$kvdnBaseUrl/R/${straddr}/${key}").headers(headers)
                .body(value)
                .asString().getBody().toString()
    }

    @Override
    // there is also submit uuid.....
    String submit(String straddr, String value, Map<String, String> headers = [:]) {
        return Unirest.post("$kvdnBaseUrl/X/${straddr}").headers(headers)
                .body(value)
                .asString().getBody().toString()
    }

    @Override
    String get(String straddr, String key, Map<String, String> headers = [:]) {
        return Unirest.get("$kvdnBaseUrl/X/${straddr}/${key}").headers(headers)
                .asString().getBody().toString()
    }

    @Override
    String getKeys(String straddr, Map<String, String> headers = [:]) {
        return Unirest.get("$kvdnBaseUrl/KEYS/${straddr}/").headers(headers)
                .asString().getBody().toString()
    }
}
