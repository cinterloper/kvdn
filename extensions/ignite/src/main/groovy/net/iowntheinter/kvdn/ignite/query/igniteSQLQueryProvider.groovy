package net.iowntheinter.kvdn.ignite.query

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.ignite.igniteExtension
import net.iowntheinter.kvdn.storage.queryProvider
import org.apache.ignite.cache.query.QueryCursor
import org.apache.ignite.cache.query.SqlFieldsQuery

/**
 * Created by g on 1/8/17.
 */
class igniteSQLQueryProvider extends igniteExtension implements queryProvider {
    @Override
    void query(String igAddr, String query, cb) {

        vertx.executeBlocking({ future ->
            try {
                def icache = ignite.cache(igAddr)
                def sql = new SqlFieldsQuery(query)

                QueryCursor results = icache.query(sql)
                future.succeededFuture(results.getAll())
            } catch (e) {
                future.fail(e)
            }
        }, { asyncResult ->
            if (asyncResult.failed())
                cb([result: null, error: asyncResult.cause()])
            else
                cb([result: asyncResult.result(), error: null])
        })

    }

    @Override
    void load(Vertx vertx, Object o) {

        log.debug("loaded igniteSQLQueryProvider")
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
