package net.iowntheinter.kvdn.util.extensionManager.impl

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import net.iowntheinter.kvdn.util.distributedWaitGroup
import net.iowntheinter.kvdn.util.extensionManager.extension

/**
 * Created by g on 1/8/17.
 */
class loader {
    Vertx vertx
    Logger logger
    def extensions = []

    loader(Vertx vertx) {
        this.vertx = vertx

    }
//load classes that subclass the extension type


    void init(Class extensionType, cb) {

        vertx.executeBlocking({ future ->
            try {
                extensions = (new FastClasspathScanner().scan().getNamesOfSubclassesOf(extensionType))

            } catch (e) {
                future.fail(e)
            }
            future.complete extensions

        }, { asyncResult ->
            if (asyncResult.failed())
                throw asyncResult.cause()
            else {

                cb(extensions)
            }
        })

    }

    void load(extensionProcessor, cb) {

        def dwg = new distributedWaitGroup(extensions, cb, vertx)
        extensions.each { String c ->
            logger.info("loading extension $c")
            def E = this.class.classLoader.loadClass(c as String)?.newInstance() as extension
            E.load(vertx,{ //do initalization
                extensionProcessor(E, { dwg.ack(c) }) //allow user to call specalized methods
            })
        }
    }


}


