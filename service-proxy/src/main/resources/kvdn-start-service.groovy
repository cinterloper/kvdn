//this will start the server standalone, if you execute the jar, on port 9090
//you can also just include the jar in your project, in which case this file will not run, but you can do it yourself

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.shiro.ShiroAuthOptions
import io.vertx.ext.auth.shiro.ShiroAuthRealmType
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.serviceproxy.ServiceBinder
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl
import net.iowntheinter.kvdn.service.KvdnService
//import net.iowntheinter.kvdn.service.kvsvcVertxProxyHandler;

(org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger).
        setLevel(Level.TRACE)
v = vertx as Vertx //get the instance of the java class

//s = new kvserver(v)
logger = new LoggerFactory().getLogger("kvdn")

router = Router.router(v)
//router.route().handler(BodyHandler.create())
JsonObject config = new JsonObject().put("properties_path", "classpath:test-auth.properties")

def opts = new ShiroAuthOptions().setConfig(config).setType(ShiroAuthRealmType.PROPERTIES)
//AuthProvider provider = ShiroAuth.create(v, opts);



SockJSHandler sjsh = SockJSHandler.create(v)
def options = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions()
        .setAddressRegex(".*"))
        .addInboundPermitted(new PermittedOptions()
        .setAddressRegex(".*"))

sjsh.bridge(options)



router.route("/eventbus/*").handler(sjsh)
router.route().handler(BodyHandler.create())

//router.route("/KVDN/*").handler(BasicAuthHandler.create(provider))
//router.route("/loginhandler").handler(FormLoginHandler.create(provider));

s.init(router as Router, { AsyncResult result ->
 //   router.route().handler(StaticHandler.create())

   // router.route().handler(CookieHandler.create())
  //  router.route().handler(SessionHandler.create(LocalSessionStore.create(v)))
  //  router.route().handler(UserSessionHandler.create(provider))
// Any requests to URI starting '/private/' require login
//router.route("/KVDN/*").handler(RedirectAuthHandler.create(provider, "/loginpage.html"));
    //router.route("/eb/*").handler(BasicAuthHandler.create(provider))
   /* router.route("/logout").handler({context ->
        context.clearUser()
        // Redirect back to the index page
        context.response().putHeader("location", "/").setStatusCode(302).end()
    })*/
    try {

        def server = v.createHttpServer()

        server.requestHandler(router.&accept).listen(6501)
    } catch (e) {
        logger.error "could not setup http server:" + e.getMessage()
    }

    def svc = new KvdnServiceImpl(v)
    svc.setup(new Handler() {
        @Override
        void handle(Object event) {
            LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl complete")
            new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class,svc as KvdnServiceImpl)

        }
    })//possiable race condition


})