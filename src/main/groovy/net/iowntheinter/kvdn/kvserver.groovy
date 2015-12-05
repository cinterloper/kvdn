package net.iowntheinter.kvdn

import com.google.common.collect.Multimap
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.BodyHandler
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kv.impl.kvdnSession

class kvserver {
    def logger
    def eb
    def vertx
    def router
    def sockJSHandler
    def config
    def ctx
    def session
    def kvserver(p, v) { //default constructer for creating our own router
        vertx = v
        router = Router.router(v)
        ctx = vertx.getOrCreateContext()
        config = new JsonObject(ctx.config())
        sockJSHandler = SockJSHandler.create(v)
        def options = [:]
        sockJSHandler.bridge(options)
        router.route().handler(BodyHandler.create())
        router.route("/kvbus/*").handler(sockJSHandler)
        session = new kvdnSession(vertx)
        start(p, router, v)
    }
    //if being used as a library, we create the vertx instance.
    def kvserver(c) {
        config = c
        vertx = Vertx.vertx()
        sockJSHandler = SockJSHandler.create(v)
        def options = [:]
        sockJSHandler.bridge(options)
            router.route().handler(BodyHandler.create())
        router.route("/kvbus/*").handler(sockJSHandler)
        start(c.getJsonObject("System").getInteger("Port"), router, vertx)
    }

    def start(p, r, v) { //real initializaion function
        logger = new LoggerFactory().getLogger("kvdn")
        eb = v.eventBus()

        r.delete("/X/:str/:map/:key").handler(this.&handleMapDel)
        //r.post("/:str/:map").handler(this.&handleMapPost)
        r.get("/X/:str/:map/:key").handler(this.&handleMapGet)
        r.get("/KEYS/:str/:map/").handler(this.&handleMapKeys)
        r.put("/X/:str/:map/:key").handler(this.&handleMapSet)

        try {
            def server = v.createHttpServer()
            server.requestHandler(router.&accept).listen(p)
        } catch (e) {
            logger.error "could not setup http server:" + e.getMessage()
        }

    }

    def handleMapKeys(routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).end()
        } else {
            def tx = session.newTx("${sName}:${mName}")
            tx.getKeys( { resGetK ->
                if (resGetK.getString('error') == null) {
                    response.end(resGetK.getJsonArray("result").toString())
                } else {
                    response.setStatusCode(501).end(resGetK.error)
                }
            })

        }
    }

    def handleMapGet(routingContext) {
        def mName = routingContext.request().getParam("map")
        def kName = routingContext.request().getParam("key")
        def sName = routingContext.request().getParam("str")

        def response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).end()
        } else {
            def tx = session.newTx("${sName}:${mName}")
            tx.get(kName, { resGet ->
                if (resGet.error == null) {
                    response.end(resGet.result.toString())
                } else {
                    response.setStatusCode(501).end(resGet.error)
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
                def tx = session.newTx("${sName}:${mName}")
                tx.set(kName, content, { resPut ->
                    if (resPut.error == null) {
                        response.end(mName + ":" + kName)
                    } else {
                        response.setStatusCode(501).end(resPut.error)
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
            response.setStatusCode(400).end()
        } else {
            def tx = session.newTx("${sName}:${mName}")

            tx.del(kName, { resDel ->
                        if (resDel.error == null) {
                            response.end(mName + ":" + kName)
                        } else {
                            response.setStatusCode(501).end(resDel.error)
                        }
                    })

        }
    }

}
