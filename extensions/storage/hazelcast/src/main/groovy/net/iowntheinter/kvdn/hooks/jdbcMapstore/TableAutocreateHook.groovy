package net.iowntheinter.kvdn.hooks.jdbcMapstore


import groovy.text.SimpleTemplateEngine
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.hazelcast.HzExtension
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.util.ResourceLoader

/**
 * Created by g on 1/9/17.
 */
class TableAutocreateHook extends HzExtension implements TXNHook {
    final JDBCClient client

    TableAutocreateHook() {
        client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", System.getenv("HZ_MAPSTORE_JDBC_DBURL"))
                .put("driver_class", "org.postgresql.Driver")
                .put("max_pool_size", 30))

    }

    @Override
    void load(Vertx vertx, Handler handler) {

        handler.handle(Future.succeededFuture())
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

    @Override
    void call(KvdnOperation KvdnOperation, KvdnSession kvdnSession, Handler handler) {
        def strAddr = KvdnOperation.strAddr
        def binding = [TABLE_NAME: strAddr]
        def e = new SimpleTemplateEngine()

        def r = new ResourceLoader()





        if (!kvdnSession.accessCache.keySet().contains(strAddr)) {
            String query = e.createTemplate(r.getResource("pgsql.template"))
                    .make(binding)
            client.getConnection({ ar ->
                if (ar.failed()) {
                    logger.fatal(ar.cause())
                } else {

                    def conn = ar.result()

                    conn.query(query, { queryResult ->
                        cb(queryResult)
                    })
                }

            })

        }
    }

    @Override
    HookType getType() {
        return null
    }
}
