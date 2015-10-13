package net.iowntheinter.kvdn

import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.shareddata.AsyncMap
import io.vertx.groovy.core.shareddata.SharedData
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.BodyHandler
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler


class server {
    def logger
    def sd
    def eb
    def vertx
    def router
    def sockJSHandler
    def server(port,v) {
        vertx=v;
        router = Router.router(vertx)
        sockJSHandler = SockJSHandler.create(vertx)
        def options = [:]
        logger = new LoggerFactory().getLogger("kvdn")
        sd = vertx.sharedData() as io.vertx.core.shareddata.SharedData
        eb = vertx.eventBus()
        sockJSHandler.bridge(options)
        router.route().handler(BodyHandler.create())
        router.route("/eventbus/*").handler(sockJSHandler)
        router.delete("/:str/:map/:key").handler(this.&handleMapDel)
        //router.post("/:str/:map").handler(this.&handleMapPost)
        router.get("/:str/:map/:key").handler(this.&handleMapGet)
        router.put("/:str/:map/:key").handler(this.&handleMapSet)
        vertx.createHttpServer().requestHandler(router.&accept).listen(port)
    }
        
    def handleMapGet(routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            sd.getClusterWideMap("${sName}:${mName}", { res ->
                if (res.succeeded()) {
                    def AsyncMap map = res.result();
                    map.get(kName, { resGet ->
                        if (resGet.succeeded()) {
                            response.end(resGet.result().toString())
                            logger.info("get:${sName}:${mName}:${kName}");
                        } else {
                            response.setStatusCode(501).end()
                        }
                    })
                } else {
                    response.setStatusCode(502).end()
                }
            })
        }
    }


    def handleMapSet(routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")
        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            def content;
            def JsonObject entry = routingContext.getBodyAsJson()
            try {
                content = entry.getJsonObject("content").toString()
            } catch (ClassCastException e) {
                content = entry.getString("content").toString()
            }
            if (entry == null) {
                response.sendError(400, response.toString())
            } else {
                sd.getClusterWideMap("${sName}:${mName}", { res ->

                    if (res.succeeded()) {
                        def AsyncMap map = res.result();
                        map.put(kName, content, { resPut ->
                            if (resPut.succeeded()) {
                                response.end(mName + ":" + kName)
                                eb.publish("${sName}:${mName}", kName)
                                logger.info("set:${sName}:${mName}:${kName}");
                                //notify those subscribing to the map that a key has ben updated
                            } else {
                                response.setStatusCode(501).end()
                            }
                        })
                    } else {
                        response.setStatusCode(502).end()
                    }
                })
            }
        }
    }

    def handleMapDel(routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end
        } else {
            sd.getClusterWideMap("${sName}:${mName}", { res ->
                if (res.succeeded()) {
                    def AsyncMap map = res.result();
                    map.remove(kName, { resDel ->
                        if (resDel.succeeded()) {
                            response.end(mName + ":" + kName)
                            eb.publish("-${sName}:${mName}", kName)
                            logger.info("del:${sName}:${mName}:${kName}");

                        } else {
                            response.setStatusCode(501).end()
                        }
                    })
                } else {
                    response.setStatusCode(502).end()
                }
            })
        }
    }

}