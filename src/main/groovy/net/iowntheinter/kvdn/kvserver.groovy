package net.iowntheinter.kvdn

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.Router
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kvdnSession

class kvserver {
    final String version
    final JsonObject config
    Logger logger
    EventBus eb
    Vertx vertx
    Router router
    Context ctx
    def session
    def _token = '_'
    //used in a vertx program, or standalone
    def kvserver(Vertx vertx) {
        logger = new LoggerFactory().getLogger("kvdn")
        this.vertx = vertx
        ctx = vertx.getOrCreateContext()
        config = ctx.config() as JsonObject
        session = new kvdnSession(vertx)
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        this.version=(classloader.getResourceAsStream('__VERSION.txt').getText())
    }

    def init(Router r, cb) { //real initializaion function
        router = r
        session.init({

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

            cb();

        }, { Exception e -> logger.fatal(e) })

    }
    String filterAddr(String s){
        return s.replace('.','') //make this pluggable?
    }

    def handleMapKeys(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))

        def response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).end()
        } else {
            KvTx tx = session.newTx("${sName}${_token}${mName}")
            tx.getKeys({ resGetK ->
                if (resGetK.error == null) {
                    response.end(new JsonArray(resGetK.result as List).toString())
                } else {
                    response.setStatusCode(501).end(resGetK.error.toString())
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
            KvTx tx = session.newTx("${sName}${_token}${mName}")
            tx.size({ resSize ->
                if (resSize.error == null) {
                    response.end(resSize.result.toString())
                } else {
                    response.setStatusCode(501).end(resSize.error.toString())
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
            KvTx tx = session.newTx("${sName}${_token}${mName}")
            tx.get(kName, { resGet ->
                if (resGet.error == null) {
                    response.end(resGet.result.toString())
                } else {
                    response.setStatusCode(501).end(resGet.error.toString())
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
            String content;
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    response.setStatusCode(400).end()
                } else {
                    KvTx tx = session.newTx("${sName}${_token}${mName}")
                    tx.set(kName as String, content, { resPut ->
                        if (resPut.error == null) {
                            response.end(mName + ":" + kName)
                        } else {
                            response.setStatusCode(501).end(resPut.error.toString())
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

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String content = routingContext.getBodyAsString()
            if (content == null) {
                response.setStatusCode(400).end(response.toString())
            } else {
                KvTx tx = session.newTx("${sName}${_token}${mName}")
                tx.set(kName as String, content, { resPut ->
                    if (resPut.error == null) {
                        response.end(mName + ":" + kName)
                    } else {
                        response.setStatusCode(501).end(resPut.error.toString())
                    }
                })
            }

        }
    }

    def handleMapSubmit(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def sName = filterAddr(routingContext.request().getParam("str"))
        def response = routingContext.response()
        if (mName == null) {
            response.setStatusCode(400).end()
        } else {
            String content;
            try {
                content = routingContext.getBodyAsString()

                if (content == null) {
                    response.setStatusCode(400).end()
                } else {
                    KvTx tx = session.newTx("${sName}${_token}${mName}")
                    tx.submit(content, { resPut ->
                        if (resPut.error == null) {
                            response.end(mName + ":" + resPut.key)
                        } else {
                            response.setStatusCode(501).end(resPut.error.toString())
                        }
                    })
                }
            } catch (Exception e) {
                response.setStatusCode(501).end(e.getMessage())
            }

        }
    }

    def handleMapDel(RoutingContext routingContext) {
        def mName = filterAddr(routingContext.request().getParam("map"))
        def kName = routingContext.request().getParam("key")
        def sName = filterAddr(routingContext.request().getParam("str"))
        logger.info("full uri on delete: " + routingContext.request().absoluteURI())
        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            KvTx tx = session.newTx("${sName}${_token}${mName}")

            tx.del(kName, { resDel ->
                if (resDel.error == null) {
                    if (!response.ended()) //why ?
                        response.end(mName + ":" + kName)
                } else {
                    response.setStatusCode(501).end(resDel.error.toString())
                }
            })

        }
    }

}
