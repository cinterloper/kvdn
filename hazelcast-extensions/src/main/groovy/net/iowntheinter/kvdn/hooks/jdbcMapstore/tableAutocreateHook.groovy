package net.iowntheinter.kvdn.hooks.jdbcMapstore

import groovy.text.SimpleTemplateEngine
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import net.iowntheinter.cornerstone.util.resourceLoader
import net.iowntheinter.kvdn.hazelcast.hzExtension
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook;

/**
 * Created by g on 1/9/17.
 */
class tableAutocreateHook extends hzExtension implements txnHook {
    final JDBCClient client

    tableAutocreateHook() {
        client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", System.getenv("HZ_MAPSTORE_JDBC_DBURL"))
                .put("driver_class", "org.postgresql.Driver")
                .put("max_pool_size", 30));

    }

    @Override
    void load(Vertx vertx, cb) {
        cb()
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

    @Override
    void call(kvdnTX kvdnTX, kvdnSession kvdnSession, cb) {
        def strAddr = kvdnTX.strAddr
        def binding = [TABLE_NAME: strAddr]
        def e = new SimpleTemplateEngine()

        def r = new resourceLoader()





        if (!kvdnSession.accessCache.keySet().contains(strAddr)) {
            String query = e.createTemplate(r.getResource("pgsql.template"))
                    .make(binding)
            client.getConnection({ ar ->
                if (ar.failed()) {
                    logger.fatal(ar.cause())
                } else {

                    def conn = ar.result()

                    conn.


                    conn.query(query, { queryResult ->
                        cb(queryResult)
                    })
                }

            })

        }
    }
}
