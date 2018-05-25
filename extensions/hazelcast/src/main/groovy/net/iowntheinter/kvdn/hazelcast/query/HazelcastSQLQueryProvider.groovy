package net.iowntheinter.kvdn.hazelcast.query

import com.hazelcast.core.IMap
import com.hazelcast.query.SqlPredicate
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.hazelcast.HzExtension
import net.iowntheinter.kvdn.query.QueryProvider

/**
 * Created by g on 1/8/17.
 */
class HazelcastSQLQueryProvider extends HzExtension implements QueryProvider {

    void query(String s, JsonObject jsonObject, Handler<AsyncResult<JsonObject>> handler) {
        vertx.executeBlocking({ future ->
            try {
                IMap map = client.getMap(hzstraddr)
                Set results = map.values(new SqlPredicate(query))
                future.succeededFuture(results)
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
    void load(Vertx vertx, Handler handler) {

        handler.handle(Future.succeededFuture())
    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }

}
