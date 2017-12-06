package net.iowntheinter.kvdn.util.extensionManager.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import net.iowntheinter.kvdn.util.distributedWaitGroup
import net.iowntheinter.kvdn.util.extensionManager.extension

import java.util.function.BiFunction
import java.util.function.Function


/**
 * Created by g on 1/8/17.
 */
@CompileStatic
@TypeChecked
class loader {
    Vertx vertx
    Logger logger
    Set<String> extensions = []

    loader(Vertx vertx) {
        this.vertx = vertx

    }
//load classes that subclass the extension type


    void init(Class extensionType, Handler cb) {

        vertx.executeBlocking({ Future future ->
            try {
                extensions = (new FastClasspathScanner().scan().getNamesOfSubclassesOf(extensionType)).toSet()
            } catch (e) {
                future.fail(e)
            }
            future.complete extensions

        }, { AsyncResult asyncResult ->
            if (asyncResult.failed())
                throw asyncResult.cause()
            else {

                cb.handle(extensions)
            }
        })

    }

    void load(Closure extensionProcessor, Handler cb) {

        def dwg = new distributedWaitGroup(extensions, cb, vertx)
        extensions.each { String c ->
            logger.info("loading extension $c")
            def E = this.class.classLoader.loadClass(c as String)?.newInstance() as extension
            E.load(vertx, { //do initalization
                extensionProcessor(E, { dwg.ack(c) }) //allow user to call specalized methods
            })
        }
    }


}


