package net.iowntheinter.kvdn

import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kv.impl.kvdnSession
import io.vertx.ext.web.handler.sockjs.BridgeOptions

class kvserver {
    def Logger logger
    def EventBus eb
    def Vertx vertx
    def Router router
    def sjsh
    def JsonObject config
    def Context ctx
    def session
    //used in a vertx program, or standalone
    def kvserver() {

    }
    //if being used as a library, create our own Vert.x instance.
    def kvserver(c) {
        config = c
        vertx = Vertx.vertx()
        sjsh = SockJSHandler.create(v)
        def options = [:]
        sjsh.bridge(options)
        router.route().handler(BodyHandler.create())
        router.route("/eb/*").handler(sjsh)

        init(router, vertx, {
            logger.info("initalized vertx and kvdn")
        });
    }

    def init(Router r, Vertx v, cb) { //real initializaion function
        Vertx vertx = v
        router = r
        ctx = vertx.getOrCreateContext()
        config = ctx.config() as JsonObject

        session = new kvdnSession(vertx)

        logger = new LoggerFactory().getLogger("kvdn")
        eb = v.eventBus()
        def prefix = ""
        if(config.containsKey('kvdn_prefix'))
            prefix="/${config.getString('kvdn_prefix')}"


        r.delete("${prefix}/X/:str/:map/:key").handler(this.&handleMapDel)
        r.post("${prefix}/X/:str/:map").handler(this.&handleMapSubmit)
        r.post("${prefix}/U/:str/:map").handler(this.&handleMapSubmitUUID)
        r.get("${prefix}/X/:str/:map/:key").handler(this.&handleMapGet)
        r.get("${prefix}/KEYS/:str/:map/").handler(this.&handleMapKeys)
        r.put("${prefix}/X/:str/:map/:key").handler(this.&handleMapSet)
        r.put("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)
        r.post("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)

        cb();

    }

    def handleMapKeys(RoutingContext routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).end()
        } else {
            KvTx tx = session.newTx("${sName}:${mName}")
            tx.getKeys({ resGetK ->
                if (resGetK.error == null) {
                    response.end(resGetK.result.toString())
                } else {
                    response.setStatusCode(501).end(resGetK.error.toString())
                }
            })

        }
    }

    def handleMapGet(RoutingContext routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            KvTx tx = session.newTx("${sName}:${mName}")
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

        def mName = routingContext.request().getParam("map")
        def sName = routingContext.request().getParam("str")
        def kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            def content;
            try {
                def JsonObject entry = routingContext.getBodyAsJson()
                try {
                    content = entry.getJsonObject("content").toString()
                } catch (ClassCastException e) {
                    content = entry.getString("content").toString()
                }
                if (entry == null) {
                    response.setStatusCode(400).end()
                } else {
                    KvTx tx = session.newTx("${sName}:${mName}")
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

        def mName = routingContext.request().getParam("map")
        def sName = routingContext.request().getParam("str")
        def kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            String content = routingContext.getBodyAsString()
            if (content == null) {
                response.setStatusCode(400).end(response.toString())
            } else {
                KvTx tx = session.newTx("${sName}:${mName}")
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
        def mName = routingContext.request().getParam("map")
        def sName = routingContext.request().getParam("str")
        def response = routingContext.response()
        if (mName == null) {
            response.setStatusCode(400).end()
        } else {
            def content;
            try {
                def JsonObject entry = new JsonObject(routingContext.getBodyAsString())
                try {
                    content = entry.getJsonObject("content").toString()
                } catch (ClassCastException e) {
                    content = entry.getString("content").toString()
                }
                if (entry == null) {
                    response.setStatusCode(400).end()
                } else {
                    KvTx tx = session.newTx("${sName}:${mName}")
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
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")
        logger.info("full uri on delete: " + routingContext.request().absoluteURI())
        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            KvTx tx = session.newTx("${sName}:${mName}")

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