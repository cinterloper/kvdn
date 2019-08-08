package net.iowntheinter.kvdn

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.util.DistributedWaitGroup
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Example of an asynchronous unit test written in JUnit style using vertx-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

@RunWith(VertxUnitRunner.class)
class Titerator {

    Vertx vertx = Vertx.vertx()
    def kvs
    String addr, key, data
    Logger logger = LoggerFactory.getLogger(this.class.getName())

    @Before
    void before(TestContext context) {

        vertx.exceptionHandler(context.exceptionHandler())
        kvs = new KvdnSession(vertx) as KvdnSession
        addr = UUID.randomUUID().toString()

    }

    @After
    void after(TestContext context) {
        //vertx.close(context.asyncAssertSuccess());
    }


    @Test
    void batch_insert_then_iterate(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def s = new KvdnSession(vertx)
        s.init({

            def tokens = ['1', '2', '3']
            def bucket = []
            def d = new DistributedWaitGroup(tokens.toSet(), {
                logger.info("wait group got tokens, starting newIterator")
                s.newIterator('this/that', { AsyncResult fin ->
                    assert (bucket as Set) == (tokens as Set)
                    async.complete()
                }).iterate({ MapEntry data, Handler<AsyncResult> cb ->
                    logger.info("inside newIterator")
                    logger.info "${data.key}:${data.value}"
                    bucket.push(data.value)
                    cb.handle(Future.succeededFuture())
                })

            }, vertx)
            def m = 'this/that'
            d.onKeys(m, s)
            tokens.each { String token ->
                def t = s.newOp('this/that') as KvOp
                println("setting ${token}")
                t.set(token, (token), { AsyncResult ar ->
                    logger.info("set ${token}")
                    if (!ar.succeeded())
                        throw ar.cause()
                })

            }

        }, {
            context.fail()
        })
    }
}