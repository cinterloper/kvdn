package net.iowntheinter.kvdn.transaction

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

interface KVDNTransaction {

    // void newTransation(Handler<AsyncResult<UUID>> cb)
    //straddr:key ?
    void acquireResources(UUID txid, Map<String, String> resources, Handler<AsyncResult> cb)

    void addOperation()//??


    void execute(Handler<AsyncResult> cb)


}
