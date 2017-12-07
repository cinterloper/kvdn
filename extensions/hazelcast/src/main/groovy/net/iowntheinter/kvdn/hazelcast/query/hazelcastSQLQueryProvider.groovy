package net.iowntheinter.kvdn.hazelcast.query

import com.hazelcast.core.IMap
import com.hazelcast.query.SqlPredicate
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.hazelcast.hzExtension
import net.iowntheinter.kvdn.storage.queryProvider

/**
 * Created by g on 1/8/17.
 */
class hazelcastSQLQueryProvider extends hzExtension implements queryProvider {

    void query(String hzstraddr, String query, cb) {
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
    void load(Vertx vertx, Object o) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
