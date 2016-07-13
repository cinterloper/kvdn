package net.iowntheinter.kvdn.http.example
import io.vertx.ext.web.Router
import net.iowntheinter.kvdn.http.routeProvider

/**
 * Created by g on 7/12/16.
 */
class helloWorldRouteProvider implements routeProvider  {
    @Override
    void addRoutes(Router router) {
        router.get('/hello').blockingHandler({ request ->
            request.response().end("hello!")
        })
    }
}
