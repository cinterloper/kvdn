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
    //@todo implement transaction control (rate limit, etc)

    HTTPServer(Vertx vertx) {
        this(vertx, new KvdnServiceImpl(vertx))
    }

    HTTPServer(Vertx vertx, KvdnServiceImpl svc) {
        this.svc = svc
        logger = new LoggerFactory().getLogger("kvdn")
        this.vertx = vertx
        ctx = vertx.getOrCreateContext()
        config = ctx.config() as JsonObject
        ClassLoader classloader = (Thread.currentThread().getContextClassLoader())
        this.version = (classloader.getResourceAsStream('_KVDN_VERSION.json').getText())
    }

    void init(Map<String,Object> config, Handler<AsyncResult> cb) {
        throw new Exception("you need to pass a router object")
    } //@todo implement this

    void init(Router r, Handler<AsyncResult> cb) {
        LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl inside HTTPServer")
        new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class, svc as KvdnServiceImpl)
        init(r, svc, cb)
    }


    void init(Router r, KvdnServiceImpl svc, Handler<AsyncResult> cb) {
        router = r

        this.svc = svc

        new ServiceBinder(vertx).setAddress("kvdnsvc").register(KvdnService.class, svc as KvdnServiceImpl)
        LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl complete")
        eb = vertx.eventBus()
        String prefix = ""
        if (config.containsKey('kvdn_prefix'))
            prefix = "/${config.getString('kvdn_prefix')}"
        if (config.containsKey('kvdn_seperator_token'))
            _token = "${config.getString('kvdn_seperator_token')}"

        //this is the whole http api.
        r.delete("${prefix}/X/:str/:map/:key").handler(this.&handleMapDel)
      //  r.delete("${prefix}/Q/:str/:que").handler(this.&handleQueGC)
        r.post("${prefix}/X/:str/:map").handler(this.&handleMapSubmit)
        r.post("${prefix}/Q/:str/:que").handler(this.&handleQueSubmit)
        r.post("${prefix}/X/:str/:map/").handler(this.&handleMapSubmitBulk)
        r.post("${prefix}/U/:str/:map").handler(this.&handleMapSubmitUUID)
        r.post("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)
        r.put("${prefix}/X/:str/:map/:key").handler(this.&handleMapSet)
        r.put("${prefix}/R/:str/:map/:key").handler(this.&handleMapSetRaw)
        r.put("${prefix}/CAG/:str/:ctr").handler(this.&handleCtrAddAndGet)
        r.put("${prefix}/CGA/:str/:ctr").handler(this.&handleCtrGetAndAdd)
        r.put("${prefix}/CAS/:str/:ctr").handler(this.&handleCtrCAS)
        r.get("${prefix}/X/:str/:map/:key").handler(this.&handleMapGet)
        r.get("${prefix}/C/:str/:ctr").handler(this.&handleCtrGet)
        r.get("${prefix}/Q/:str/:que").handler(this.&handleQueGet)
        r.get("${prefix}/Q_/:str/:que").handler(this.&handleQueStat)
        r.get("${prefix}/KEYS/:str/:map/").handler(this.&handleMapKeys)
        r.get("${prefix}/SIZE/:str/:map/").handler(this.&handleMapSize)
        r.get("${prefix}/__VERSION").handler({ RoutingContext rc ->
            rc.response().putHeader("content-type", "text/plain").end(this.version)
        })
        cb.handle(Future.succeededFuture())


    }

    static String filterAddr(String s) {
        return s.replace('.', '') //make this pluggable?
    }


    void handleCtrGet(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("ctr"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "application/json").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.ctrGet(straddr, new JsonObject(), { AsyncResult<Long> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "application/json")
                    response.putHeader("content-type", "text/plain").end(resGetK.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGetK.cause().toString())
                }
            })

        }
    }

    void handleCtrAddAndGet(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("ctr"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        Long value
        try {
            value = routingContext.queryParams().get("value").toLong()
        } catch (ClassCastException e) {
            routingContext.fail(e)
            return
        }
        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "application/json").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.addAndGet(straddr, value, new JsonObject(), { AsyncResult<Long> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "application/json")
                    response.putHeader("content-type", "text/plain").end(resGetK.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGetK.cause().toString())
                }
            })

        }
    }

    void handleCtrCAS(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("ctr"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        Long value1, value2
        try {
            value1 = routingContext.queryParams().get("value1").toLong()
            value2 = routingContext.queryParams().get("value2").toLong()
        } catch (ClassCastException e) {
            routingContext.fail(e)
            return
        }
        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "application/json").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.ctrCompareAndSet(straddr, value1, value2, new JsonObject(), { AsyncResult<Boolean> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "text/plain").end(new JsonObject().
                            put("Succeeded", resGetK.result()).
                            put("Submitted Value", value2).toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGetK.cause().toString())
                }
            })

        }
    }

    void handleCtrGetAndAdd(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("ctr"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        Long value
        try {
            value = routingContext.queryParams().get("value").toLong()
        } catch (ClassCastException e) {
            routingContext.fail(e)
            return
        }
        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "application/json").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.getAndAdd(straddr, value, new JsonObject(), { AsyncResult<Long> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "application/json")
                    response.putHeader("content-type", "text/plain").end(resGetK.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGetK.cause().toString())
                }
            })

        }
    }


    void handleMapKeys(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "application/json").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.getKeys(straddr, new JsonObject(), { AsyncResult<JsonArray> resGetK ->
                if (resGetK.succeeded()) {
                    response.putHeader("Content-Type", "application/json")
                    response.putHeader("content-type", "text/plain").end(resGetK.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGetK.cause().toString())
                }
            })

        }
    }

    void handleMapSize(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (mName == null || sName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.size(straddr, new JsonObject(), { AsyncResult resSize ->
                if (resSize.succeeded()) {
                    response.putHeader("content-type", "text/plain").end(resSize.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resSize.cause().toString())
                }
            })

        }
    }

    void handleMapGet(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("map"))
        String kName = routingContext.request().getParam("key")
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String straddr = "${sName}${_token}${mName}"
            svc.get(straddr, kName, new JsonObject(), { AsyncResult resGet ->
                if (resGet.succeeded()) {
                    logger.trace("RESULT OF GET ${straddr} ${resGet.result().toString()}")
                    /*  def ctype = (resGet?.meta["Content-Type"]) ?: null
                      if(ctype)
                          response.putHeader("Content-Type",ctype as String)*/
                    response.putHeader("content-type", "text/plain").end(resGet.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGet.cause().toString())
                }
            })

        }
    }
    void handleQueStat(RoutingContext routingContext) {
        String qName = filterAddr(routingContext.request().getParam("que"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (qName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String straddr = "${sName}${_token}${qName}"
            svc.qPeek(straddr, new JsonObject(), { AsyncResult resGet ->
                if (resGet.succeeded()) {
                    logger.trace("RESULT OF QSTAT ${straddr} ${resGet.result().toString()}")
                    /*  def ctype = (resGet?.meta["Content-Type"]) ?: null
                      if(ctype)
                          response.putHeader("Content-Type",ctype as String)*/
                    response.putHeader("content-type", "text/plain").end(resGet.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGet.cause().toString())
                }
            })

        }
    }
    void handleQueGet(RoutingContext routingContext) {
        String qName = filterAddr(routingContext.request().getParam("que"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        HttpServerResponse response = routingContext.response()
        if (qName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String straddr = "${sName}${_token}${qName}"
            svc.dequeue(straddr, new JsonObject(), { AsyncResult resGet ->
                if (resGet.succeeded()) {
                    logger.trace("RESULT OF GET ${straddr} ${resGet.result().toString()}")
                    /*  def ctype = (resGet?.meta["Content-Type"]) ?: null
                      if(ctype)
                          response.putHeader("Content-Type",ctype as String)*/
                    response.putHeader("content-type", "text/plain").end(resGet.result().toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resGet.cause().toString())
                }
            })

        }
    }
    void handleMapSubmitUUID(RoutingContext routingContext) {
        routingContext.put('keyOverride', UUID.randomUUID().toString())
        handleMapSet(routingContext)
    }

    void handleMapSubmitBulk(RoutingContext routingContext) {
        JsonObject bulk = null
        String body = routingContext.getBodyAsString()
        body.trim()
        try {
            assert !body.isEmpty()
        } catch (e) {
            routingContext.response().setStatusCode(501).putHeader("content-type", "text/plain").end(
                    "Cannot decode bulk submit: ${e.message.toString()}".toString())
            return
        }
        try {
            if (body.charAt(0) == '{'.toCharacter()) {
                bulk = new JsonObject(body)
            } else if (body.charAt(0) == '['.toCharacter()) {
                HashMap m = new HashMap()
                int i = 0
                for (el in new JsonArray(body)) {
                    m[i] = el.toString()
                    i++
                }
                bulk = new JsonObject(m)
            } else {
                throw new Exception("does not appear to be json: ${body}")
            }
        } catch (e) {
            routingContext.response().setStatusCode(501).putHeader("content-type", "text/plain").end(
                    "Cannot decode bulk submit: ${e.message.toString()}".toString())

            return
        }

        ArrayList<Future> futures = new ArrayList<>()
        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        String straddr = "${sName}${_token}${mName}"

        for (entry in bulk) {
            String k = entry.key
            String v
            if (entry.value instanceof Map) {
                v = new JsonObject(entry.value as Map)
            } else if (entry.value instanceof ArrayList) {
                v = new JsonArray(entry.value as ArrayList)
            } else {
                v = entry.value.toString()
            }

            Future f = Future.future()
            futures.push(f)
            svc.set(straddr, k as String, v as String, new JsonObject(), { AsyncResult resPut ->
                if (resPut.succeeded()) {
                    f.succeeded()
                    f.complete()
                } else {
                    logger.error(resPut)
                    f.fail(resPut.cause())
                    f.complete()
                }
            })
        }
        CompositeFuture c = CompositeFuture.all(futures)
        c.setHandler({ AsyncResult bulkResult ->
            if (bulkResult.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("content-type", "text/plain").end()
            } else {
                routingContext.response().setStatusCode(501).putHeader("content-type", "text/plain").end(bulkResult.cause().message)
            }
        })

    }

    void handleMapSet(RoutingContext routingContext) {

        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        String kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String content
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    response.setStatusCode(400).putHeader("content-type", "text/plain").end()
                } else {
                    String straddr = "${sName}${_token}${mName}"
                    String ctype = routingContext.request().getHeader("Content-Type")
                    //if(ctype)
                    //    svc.putMeta("Content-Type",ctype)
                    svc.set(straddr, kName as String, content, new JsonObject(), { AsyncResult resPut ->
                        if (resPut.succeeded()) {
                            response.putHeader("content-type", "text/plain").end(new JsonObject().put(mName, kName).toString())
                        } else {
                            response.setStatusCode(501).putHeader("content-type", "text/plain").end(resPut.cause().toString())
                        }
                    })
                }
            } catch (e) {
                response.setStatusCode(501).putHeader("content-type", "text/plain").end(e.toString())
            }


        }
    }

    void handleMapSetRaw(RoutingContext routingContext) {

        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))
        String kName = routingContext.get('keyOverride') ?: routingContext.request().getParam("key")
        String mtype = routingContext.request().getHeader("Content-Type")

        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String content = routingContext.getBodyAsString()
            if (content == null) {
                response.setStatusCode(400).putHeader("content-type", "text/plain").end(response.toString())
            } else {
                String straddr = "${sName}${_token}${mName}"
                String ctype = routingContext.request().getHeader("Content-Type")
                // if(ctype)
                //   svc.putMeta("Content-Type",ctype)
                svc.set(straddr, kName as String, content, new JsonObject(), { AsyncResult resPut ->
                    if (resPut.succeeded()) {
                        response.putHeader("content-type", "text/plain").end(new JsonObject().put(mName, kName).toString())
                    } else {
                        response.setStatusCode(501).putHeader("content-type", "text/plain").end(resPut.cause().toString())
                    }
                })
            }

        }
    }

    void handleQueSubmit(RoutingContext routingContext) {

        String mName = filterAddr(routingContext.request().getParam("que"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        logger.trace "que submit called ${mName} ${sName}"

        HttpServerResponse response = routingContext.response()
        if (mName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String content
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    logger.trace("no content?")
                    response.setStatusCode(400).putHeader("content-type", "text/plain").end()
                } else {
                    logger.trace " creating new transaction "
                    String straddr = "${sName}${_token}${mName}"
                    String ctype = routingContext.request().getHeader("Content-Type") ?: ""
                    //if(ctype)
                    //    svc.putMeta("Content-Type",ctype)
                    svc.enqueue(straddr, new JsonObject(), content, { AsyncResult resPut ->
                        if (resPut.succeeded()) {
                            logger.trace " enqueue sucussful "
                            response.putHeader("content-type", "application/json").end(new JsonObject().put(mName, resPut.result()).toString())
                        } else {
                            logger.trace " enqueue failed"
                            response.setStatusCode(501).putHeader("content-type", "text/plain").end(resPut.cause().toString())
                        }
                    })
                }
            } catch (Exception e) {
                response.setStatusCode(501).putHeader("content-type", "text/plain").end(e.getMessage())
            }

        }
    }

    void handleMapSubmit(RoutingContext routingContext) {

        String mName = filterAddr(routingContext.request().getParam("map"))
        String sName = filterAddr(routingContext.request().getParam("str"))

        logger.trace "submit called ${mName} ${sName}"

        HttpServerResponse response = routingContext.response()
        if (mName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String content
            try {
                content = routingContext.getBodyAsString()
                if (content == null) {
                    logger.trace("no content?")
                    response.setStatusCode(400).putHeader("content-type", "text/plain").end()
                } else {
                    logger.trace " creating new transaction "
                    String straddr = "${sName}${_token}${mName}"
                    String ctype = routingContext.request().getHeader("Content-Type") ?: ""
                    //if(ctype)
                    //    svc.putMeta("Content-Type",ctype)
                    svc.submit(straddr, content, new JsonObject(), { AsyncResult resPut ->
                        if (resPut.succeeded()) {
                            logger.trace " transaction sucussful "
                            response.putHeader("content-type", "application/json").end(new JsonObject().put(mName, resPut.result()).toString())
                        } else {
                            logger.trace " transaction failed"
                            response.setStatusCode(501).putHeader("content-type", "text/plain").end(resPut.cause().toString())
                        }
                    })
                }
            } catch (Exception e) {
                response.setStatusCode(501).putHeader("content-type", "text/plain").end(e.getMessage())
            }

        }
    }

    void handleMapDel(RoutingContext routingContext) {
        String mName = filterAddr(routingContext.request().getParam("map"))
        String kName = routingContext.request().getParam("key")
        String sName = filterAddr(routingContext.request().getParam("str"))
        logger.info("full uri on delete: " + routingContext.request().absoluteURI())
        HttpServerResponse response = routingContext.response()
        if (mName == null || kName == null) {
            response.setStatusCode(400).putHeader("content-type", "text/plain").end()
        } else {
            String straddr = "${sName}${_token}${mName}"

            svc.del(straddr, kName, new JsonObject(), { AsyncResult resDel ->
                if (resDel.succeeded()) {
                    if (!response.putHeader("content-type", "text/plain").ended()) //why ?
                        response.putHeader("content-type", "text/plain").end(new JsonObject().put(mName, kName).toString())
                } else {
                    response.setStatusCode(501).putHeader("content-type", "text/plain").end(resDel.cause().toString())
                }
            })

        }
    }

}
