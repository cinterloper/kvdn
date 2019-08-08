package net.iowntheinter.kvdn.query.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.query.QueryProvider
import net.iowntheinter.kvdn.storage.KvdnSession

class QueryOperation extends KvdnOperation {

    QueryProvider QP

    QueryOperation(String straddr, String QueryProvider, UUID txid, KvdnSession session, Vertx vertx){

    }


    void query(String query, Handler<AsyncResult<JsonObject>> cb)

    {
//        startOperation(TXTYPE.KV_GET, [keys: [key]], {
//            D.getMap(this.strAddr, { AsyncResult<AsyncMap> res -> })
//        }
    }
}
