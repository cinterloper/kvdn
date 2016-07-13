package net.iowntheinter.kvdn.http

import io.vertx.ext.web.Router

interface routeProvider{

    void addRoutes(Router r)
}