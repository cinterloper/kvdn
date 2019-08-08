package net.iowntheinter.kvdn

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.storage.KvdnSession
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
class Tdwg {

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
    void test1(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def d = new DistributedWaitGroup(['1', '2', '3'].toSet(), {
            async.complete()
        }, vertx)
        d.ack('1')
        d.ack('2')
        d.ack('3')

    }

    @Test
    void test2(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def tokens = ['1', '2', '3']
        def d = new DistributedWaitGroup(tokens.toSet(), {
            async.complete()
        }, vertx)
        def c = 'achannel'
        d.onChannel(c)
        def eb = vertx.eventBus()
        tokens.each { token ->

            eb.send(c, token)

        }
    }

    @Test
    void test3(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def s = new KvdnSession(vertx)
        s.init({

            def tokens = ['1', '2', '3']
            def d = new DistributedWaitGroup(tokens.toSet(), {
                async.complete()
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