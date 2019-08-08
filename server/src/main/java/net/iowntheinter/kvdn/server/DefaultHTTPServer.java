package net.iowntheinter.kvdn.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.serviceproxy.ServiceBinder;
import net.iowntheinter.kvdn.server.impl.HTTPServer;
import net.iowntheinter.kvdn.service.KvdnService;
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DefaultHTTPServer extends AbstractVerticle {
    {
        System.out.println("initalizing default http server");
        LoggerFactory.getLogger(this.getClass().getName()).debug("constructed DefaultHTTPServer");

    }

    static CompletableFuture<Boolean> f = new CompletableFuture<Boolean>();

    //launch standalone
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DefaultHTTPServer svr = new DefaultHTTPServer(vertx);
        svr.start();
        try {
            f.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //    private final int port;
//    private final JsonObject cfg;
    private KvdnServiceImpl svc;

    public DefaultHTTPServer() {

    }

    public DefaultHTTPServer(Vertx vertx) {
        this.vertx = vertx;
//        Context x = vertx.getOrCreateContext();
//        this.cfg = x.config();
//        if (this.cfg.containsKey("KVDN_HTTPPort")) {
//            this.port = this.cfg.getInteger("KVDN_HTTPPort");
//        } else {
//            this.port = 6501;
//        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        LoggerFactory.getLogger(this.getClass().getName()).debug("started DefaultHTTPServer");
        System.out.println("started http server");

        svc = new KvdnServiceImpl(vertx);
        JsonObject cfg = vertx.getOrCreateContext().config();
        int port;
        if (cfg.containsKey("KVDN_HTTPPort")) {
            port = cfg.getInteger("KVDN_HTTPPort");
        } else {
            port = 6501;
        }



        Logger logger = LoggerFactory.getLogger(this.getClass().getName());
        HTTPServer httpserver = new HTTPServer(vertx);
        Router router = Router.router(vertx);

        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions()
                        .setAddressRegex(".*"))
                .addInboundPermitted(new PermittedOptions()
                        .setAddressRegex(".*"));

        Set<String> allowedHeaders = new HashSet<>();
        //@todo should this be in the default http server?
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        if (System.getenv("CORS_PATH") != null)
            router.route().handler(CorsHandler.create(System.getenv("CORS_PATH")).allowCredentials(true)
                    .allowedHeaders(allowedHeaders));

        SockJSHandler sjsh = SockJSHandler.create(vertx).bridge(options);

        router.route("/eventbus/*").handler(sjsh);
        router.route("/eb/*").handler(sjsh);

        router.route().handler(BodyHandler.create());

        HttpServer server = (vertx).createHttpServer();

        server.requestHandler(router::accept).listen(port);
        System.out.println("about to setup server");
        LoggerFactory.getLogger(this.getClass().getName()).info("about to setup server");
        svc.setup(new Handler<AsyncResult>() {
            @Override
            public void handle(AsyncResult event) {
                if (event.succeeded())
                    System.out.println("service setup");
                else
                    System.err.println("service setup failure: " + event.cause());


                httpserver.init(router, svc, (AsyncResult httpserverinit) -> {
                    if (httpserverinit.succeeded()) {

                        System.out.println("setup KvdnServiceImpl complete on port " + port);
                        new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class, svc);

                    } else {
                        logger.fatal(httpserverinit);
                    }
                });


            }
        });
    }

    public void stop() {
        System.out.println("HALT");
        f.complete(true);
    }
}
