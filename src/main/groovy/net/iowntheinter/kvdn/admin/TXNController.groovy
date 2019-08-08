package net.iowntheinter.kvdn.admin

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler


class TXNController {
    void mayProceed(ClientSession cs, Handler<AsyncResult> nxt){

        //@todo implement logic for backoff / ratelimiting / security
        nxt.handle(Future.succeededFuture())
    }
}
