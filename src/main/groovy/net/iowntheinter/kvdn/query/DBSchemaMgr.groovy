package net.iowntheinter.kvdn.query

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.impl.KvOp

class DBSchemaMgr {
    //schemas are immutable once created, and can only be destroyed after removeing all of their instances

    static void create(KvdnSession session, JsonObject Schema, Handler<AsyncResult> cb){
        KvOp op = session.newOp()
    }

    static void remove(KvdnSession session, Handler <AsyncResult> cb) {


    }//do we have any refrences

    static void instanceCount(Handler<AsyncResult<Integer>> cb){
        //metadata lookup
    }


}
