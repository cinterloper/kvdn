//this will start the server standalone, if you execute the jar, on port 9090
//you can also just include the jar in your project, in which case this file will not run, but you can do it yourself

import io.vertx.groovy.ext.web.Router
import net.iowntheinter.kvdn.kvserver
s = new kvserver(9090,vertx);
router = Router.router(vertx)

s.start(9090,router,vertx)