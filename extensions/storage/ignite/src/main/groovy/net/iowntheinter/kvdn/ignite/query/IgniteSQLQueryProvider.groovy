package net.iowntheinter.kvdn.ignite.query

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.ignite.IgniteExtension
import net.iowntheinter.kvdn.query.QueryProvider
import org.apache.ignite.cache.query.QueryCursor
import org.apache.ignite.cache.query.SqlFieldsQuery

/**
 * Created by g on 1/8/17.
 */
//@TypeChecked
//@CompileStatic
class IgniteSQLQueryProvider extends IgniteExtension implements QueryProvider {
    @Override
    void query(String addr, JsonObject query, Handler<AsyncResult<JsonObject>> handler) {

        vertx.executeBlocking({ Future<List<String>> future ->
            try {
                def icache = ignite.cache(addr)
                def sql = new SqlFieldsQuery(query.getString("SqlQuery"))


                QueryCursor results = icache.query(sql)
                future.succeededFuture(results.getAll() as List<String>)
            } catch (e) {
                future.fail(e)
            }
        }, { AsyncResult<List<String>> asyncResult ->
            if (asyncResult.failed())
                handler.handle(Future.failedFuture(asyncResult.cause()))
            else
                handler.handle(Future.succeededFuture(new JsonObject().put("result", new JsonArray(new ArrayList<String>(asyncResult.result())))))
        })

    }

    @Override
    void load(Vertx vertx, Handler handler) {
        log.debug("loaded IgniteSQLQueryProvider")
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

}
