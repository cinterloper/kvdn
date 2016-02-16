//this will start the server standalone, if you execute the jar, on port 9090
//you can also just include the jar in your project, in which case this file will not run, but you can do it yourself

import io.vertx.groovy.ext.web.Router
import net.iowntheinter.kvdn.kvserver
s = new kvserver();
router = Router.router(vertx)

s.init(router,vertx, {
    try {
        def server = vertx.createHttpServer()
        server.requestHandler(router.&accept).listen(9090)
    } catch (e) {
        logger.error "could not setup http server:" + e.getMessage()
    }
})