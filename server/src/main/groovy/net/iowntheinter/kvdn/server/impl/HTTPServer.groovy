package net.iowntheinter.kvdn.server.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.*
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.serviceproxy.ServiceBinder
import net.iowntheinter.kvdn.server.AbstractServer
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl
import net.iowntheinter.kvdn.service.KvdnService

@CompileStatic
@TypeChecked
class HTTPServer implements AbstractServer {
    final String version
    final JsonObject config
    Logger logger
    EventBus eb
    Vertx vertx
    Router router
    Context ctx
    String _token = '_'
    KvdnServiceImpl svc //used in a vertx program, or standalone
    HTTPServer(Vertx vertx) {
        this(vertx, new KvdnServiceImpl(vertx))
    }

    HTTPServer(Vertx vertx, KvdnServiceImpl svc) {
        this.svc = svc;
        logger = new LoggerFactory().getLogger("kvdn")
        this.vertx = vertx
        ctx = vertx.getOrCreateContext()
        config = ctx.config() as JsonObject
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        this.version = (classloader.getResourceAsStream('_KVDN_VERSION.txt').getText())
    }

    void init(Handler cb) {
        //not implemented yet
    }

    void init(Router r, Handler cb) {
        LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl inside HTTPServer")
        new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class, svc as KvdnServiceImpl)
        init(r, svc, cb)
    }

    void init(Router r, KvdnServiceImpl svc, Handler cb) {
        router = r

        this.svc = svc

        new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class, svc as KvdnServiceImpl)
        LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl complete")
        eb = vertx.eventBus()
        def prefix = ""
        if (config.containsKey('kvdn_prefix'))
            prefix = "/${config.getString('kvdn_prefix')}"
        if (config.containsKey('kvdn_seperator_token'))
            _token = "${config.getString('kvdn_seperator_token')}"

        r.delete("${prefix}/X/:str/:map/:key").handler(this.&handleMapDel)
        r.post("${prefix}/X/:str/:map").handler(this.&handleMapSubmit)
        r.post("${prefix}/U/:str/:map").handler(this.&handleMapSubmitUUID)
        r.get("${prefix}/X/:str/:map/:key").handler(this.&handleMapGet)
        r.get("${prefix}/KEYS/:str/:map/").handler(this.&handleMapKeys)
        r.get("${prefix}/SIZE/:str/:map/").handler(this.&handleMapSize)
        r.put("${prefix}/X/:str/:map/:key").handler(this.&handleMapSet)
        r.put("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)
        r.post("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)
        r.get("${prefix}/__VERSION").handler({ RoutingContext rc ->
            rc.response().end(this.version)
        })
        cb.handle(Future.succeededFuture())


    }

    static String filterAddr(String s) {
        return s.replace('.', '') //make this pluggable?
    }

    def handleMapKeys(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))

        def response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.getKeys(straddr, new JsonObject(), { AsyncResult<JsonArray> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "application/json")
                    response.end(resGetK.result().toString())
                } else {
                    response.setStatusCode(501).end(resGetK.cause().toString())
                }
            })

        }
    }

    def handleMapSize(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))

        def response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.size(straddr, new JsonObject(), { AsyncResult resSize ->
                if (resSize.succeeded()) {
                    response.end(resSize.result().toString())
                } else {
                    response.setStatusCode(501).end(resSize.cause().toString())
                }
            })

        }
    }

    def handleMapGet(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def kName = routingContext.request().getParam("key")
        def sName = filterAddr(routingContext.request().getParam("str"))

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.get(straddr, kName, new JsonObject(), { AsyncResult resGet ->
                if (resGet.succeeded()) {
                    logger.trace("RESULT OF GET ${straddr} ${resGet.result().toString()}")
                    /*  def ctype = (resGet?.meta["Content-Type"]) ?: null
                      if(ctype)
                          response.putHeader("Content-Type",ctype as String)*/
                    response.end(resGet.result().toString())
                } else {
                    response.setStatusCode(501).end(resGet.cause().toString())
                }
            })

        }
    }

    def handleMapSubmitUUID(RoutingContext routingContext) {
        routingContext.put('keyOverride', UUID.randomUUID().toString())
        handleMapSet(routingContext)
    }

    def handleMapSet(RoutingContext routingContext) {

        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))
        def kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String content
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    response.setStatusCode(400).end()
                } else {
                    String straddr = "${sName}${_token}${mName}"
                    def ctype = routingContext.request().getHeader("Content-Type")
                    //if(ctype)
                    //    svc.putMeta("Content-Type",ctype)
                    svc.set(straddr, kName as String, content, new JsonObject(), { AsyncResult resPut ->
                        if (resPut.succeeded()) {
                            response.end(new JsonObject().put(mName ,kName).toString())
                        } else {
                            response.setStatusCode(501).end(resPut.cause().toString())
                        }
                    })
                }
            } catch (e) {
                response.setStatusCode(501).end(e.toString())
            }


        }
    }

    def handleMapSetRaw(RoutingContext routingContext) {

        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))
        def kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")
        def mtype = routingContext.request().getHeader("Content-Type")

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String content = routingContext.getBodyAsString()
            if (content == null) {
                response.setStatusCode(400).end(response.toString())
            } else {
                String straddr = "${sName}${_token}${mName}"
                def ctype = routingContext.request().getHeader("Content-Type")
                // if(ctype)
                //   svc.putMeta("Content-Type",ctype)
                svc.set(straddr, kName as String, content, new JsonObject(), { AsyncResult resPut ->
                    if (resPut.succeeded()) {
                        response.end(new JsonObject().put(mName,kName).toString())
                    } else {
                        response.setStatusCode(501).end(resPut.cause().toString())
                    }
                })
            }

        }
    }

    def handleMapSubmit(RoutingContext routingContext) {

        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))
        logger.trace "submit called ${mName} ${sName}"

        def response = routingContext.response()
        if (mName == null) {
            response.setStatusCode(400).end()
        } else {
            String content
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    logger.trace("no content?")
                    response.setStatusCode(400).end()
                } else {
                    logger.trace " creating new transaction "
                    String straddr = "${sName}${_token}${mName}"
                    String ctype = routingContext.request().getHeader("Content-Type") ?: ""
                    //if(ctype)
                    //    svc.putMeta("Content-Type",ctype)
                    svc.submit(straddr, content, new JsonObject(), { AsyncResult resPut ->
                        if (resPut.succeeded()) {
                            logger.trace " transaction sucussful "
                            response.end(new JsonObject().put(mName,resPut.result()).toString())
                        } else {
                            logger.trace " transaction failed"
                            response.setStatusCode(501).end(resPut.cause().toString())
                        }
                    })
                }
            } catch (Exception e) {
                response.setStatusCode(501).end(e.getMessage())
            }

        }
    }

    def handleMapDel(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("map"))
        String kName = routingContext.request().getParam("key")
        String sName = filterAddr(routingContext.request().getParam("str"))
        logger.info("full uri on delete: " + routingContext.request().absoluteURI())
        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String straddr = "${sName}${_token}${mName}"

            svc.del(straddr, kName, new JsonObject(), { AsyncResult resDel ->
                if (resDel.succeeded()) {
                    if (!response.ended()) //why ?
                        response.end(new JsonObject().put(mName,kName).toString())
                } else {
                    response.setStatusCode(501).end(resDel.cause().toString())
                }
            })

        }
    }

}
