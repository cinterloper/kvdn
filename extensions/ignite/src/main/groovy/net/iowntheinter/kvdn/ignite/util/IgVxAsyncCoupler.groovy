package net.iowntheinter.kvdn.ignite.util

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import org.apache.ignite.lang.IgniteFuture
import io.vertx.core.Future

/**
 * Created by g on 2/20/17.
 */
class IgVxAsyncCoupler {
    void couple(IgniteFuture f, Handler<AsyncResult> h){
        f.listen({ fut ->
            def vf = Future.succeededFuture(f.get())
            h.handle(vf)
        })
    }
}
