////this will start the server standalone, if you execute the jar, on port 9090
////you can also just include the jar in your project, in which case this file will not run, but you can do it yourself
//
//import ch.qos.logback.classic.Level
//import ch.qos.logback.classic.Logger
//import io.vertx.core.json.JsonObject
//import io.vertx.ext.auth.shiro.ShiroAuthOptions
//import io.vertx.ext.auth.shiro.ShiroAuthRealmType
//import io.vertx.ext.web.Router
//import io.vertx.ext.web.handler.CookieHandler
//
//import io.vertx.ext.web.handler.BodyHandler
//import io.vertx.ext.web.handler.SessionHandler
//import io.vertx.ext.web.handler.StaticHandler
//import io.vertx.ext.web.handler.sockjs.BridgeOptions
//import io.vertx.ext.web.handler.sockjs.SockJSHandler
//import io.vertx.ext.web.handler.sockjs.PermittedOptions
//import io.vertx.core.logging.LoggerFactory
//import io.vertx.ext.web.sstore.LocalSessionStore
//import kvserver
//import io.vertx.core.Vertx
//import io.vertx.core.Vertx as GVertx
//(org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger).
//        setLevel(Level.TRACE)
//v = (vertx as GVertx).getDelegate() as Vertx //take the instance of the java class
//
//s = new HTTPServer(v)
//logger = new LoggerFactory().getLogger("kvdn")
//
//router = Router.router(v)
//router.route().handler(BodyHandler.create())
//JsonObject config = new JsonObject().put("properties_path", "classpath:test-auth.properties")
//
//def opts = new ShiroAuthOptions().setConfig(config).setType(ShiroAuthRealmType.PROPERTIES)
////AuthProvider provider = ShiroAuth.create(v, opts);
//
//
//
//def sjsh = SockJSHandler.create(v)
//def options = new BridgeOptions()
//        .addOutboundPermitted(new PermittedOptions()
//        .setAddressRegex(".*"))
//        .addInboundPermitted(new PermittedOptions()
//        .setAddressRegex(".*"))
//sjsh.bridge(options)
//
//
//
//router.route("/eb/*").handler(sjsh)
////router.route("/KVDN/*").handler(BasicAuthHandler.create(provider))
////router.route("/loginhandler").handler(FormLoginHandler.create(provider));
//
//s.init(router as Router, {
//    router.route().handler(StaticHandler.create())
//
//    router.route().handler(CookieHandler.create())
//    router.route().handler(BodyHandler.create())
//    router.route().handler(SessionHandler.create(LocalSessionStore.create(v)))
//  //  router.route().handler(UserSessionHandler.create(provider))
//// Any requests to URI starting '/private/' require login
////router.route("/KVDN/*").handler(RedirectAuthHandler.create(provider, "/loginpage.html"));
//    //router.route("/eb/*").handler(BasicAuthHandler.create(provider))
//    router.route("/logout").handler({context ->
//        context.clearUser()
//        // Redirect back to the index page
//        context.response().putHeader("location", "/").setStatusCode(302).end()
//    })
//    try {
//
//        def server = v.createHttpServer()
//
//        server.requestHandler(router.&accept).listen(6500)
//    } catch (e) {
//        logger.error "could not setup http server:" + e.getMessage()
//    }
//
//})
