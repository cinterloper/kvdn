//this will start the server standalone, if you execute the jar, on port 9090
//you can also just include the jar in your project, in which case this file will not run, but you can do it yourself

import io.vertx.ext.web.Router
import net.iowntheinter.kvdn.kvserver
import io.vertx.core.Vertx
import io.vertx.groovy.core.Vertx as GVertx

s = new kvserver();
v = (vertx as GVertx).getDelegate() as Vertx //get the instance of the java class

router = Router.router(v)


s.init(router,v, {
    try {
        def server = v.createHttpServer()
        server.requestHandler(router.&accept).listen(9090)
    } catch (e) {
        logger.error "could not setup http server:" + e.getMessage()
    }
})