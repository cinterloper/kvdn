//this will start the server standalone, if you execute the jar, on port 9090
//you can also just include the jar in your project, in which case this file will not run, but you can do it yourself

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.core.logging.LoggerFactory

import net.iowntheinter.kvdn.kvserver
import io.vertx.core.Vertx
import io.vertx.groovy.core.Vertx as GVertx

s = new kvserver();
v = (vertx as GVertx).getDelegate() as Vertx //get the instance of the java class
logger = new LoggerFactory().getLogger("kvdn")

router = Router.router(v)
router.route().handler(BodyHandler.create())


s.init(router,v, {
    try {
        def server = v.createHttpServer()
        def sjsh = SockJSHandler.create(v)
        def options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
        sjsh.bridge(options)
        router.route("/eb/*").handler(sjsh)
        server.requestHandler(router.&accept).listen(9090)
    } catch (e) {
        logger.error "could not setup http server:" + e.getMessage()
    }
})