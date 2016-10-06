/**
 * Created by g on 9/17/16.
 */
import io.vertx.ext.web.Router

import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.core.logging.LoggerFactory
import io.vertx.serviceproxy.ProxyHelper
import net.iowntheinter.kvdn.kvserver
import io.vertx.core.Vertx
import io.vertx.groovy.core.Vertx as GVertx
import net.iowntheinter.kvdn.service.impl.kvdnService
import net.iowntheinter.kvdn.service.kvsvc

s = new kvserver();
v = (vertx as GVertx).getDelegate() as Vertx //get the instance of the java class
logger = new LoggerFactory().getLogger("kvdn")

router = Router.router(v)
router.route().handler(BodyHandler.create())


s.init(router as Router, {
    try {
        def server = v.createHttpServer()
        def sjsh = SockJSHandler.create(v)
        def options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions()
                .setAddressRegex(".*")).addInboundPermitted(new PermittedOptions()
                .setAddressRegex(".*"));
        sjsh.bridge(options)
        router.route("/eb/*").handler(sjsh)
        server.requestHandler(router.&accept).listen(6500)
    } catch (e) {
        logger.error "could not setup http server:" + e.getMessage()
    }
})

def svc = new kvdnService(v)
ProxyHelper.registerService(kvsvc.class, v, svc, "kvdnsvc")