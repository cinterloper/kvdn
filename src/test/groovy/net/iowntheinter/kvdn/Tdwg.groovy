package net.iowntheinter.kvdn

import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.util.distributedWaitGroup
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
        kvs = new kvdnSession(vertx) as kvdnSession
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
        def d = new distributedWaitGroup(['1','2','3'].toSet(),{
            async.complete()
        },vertx)
        d.ack('1')
        d.ack('2')
        d.ack('3')

    }
    @Test
    void test2(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def tokens=['1','2','3']
        def d = new distributedWaitGroup(tokens.toSet(),{
            async.complete()
        },vertx)
        def c = 'achannel'
        d.onChannel(c)
        def eb = vertx.eventBus()
        tokens.each { token ->

            eb.send(c,token)

        }
    }
    @Test
    void test3(TestContext context) {
        Async async = context.async()
        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        def s = new kvdnSession(vertx)
        s.init({

            def tokens=['1','2','3']
            def d = new distributedWaitGroup(tokens.toSet(),{
                async.complete()
            },vertx)
            def m = 'this/that'
            d.onKeys(m,s)
            tokens.each { token ->
                def t = s.newTx('this/that') as KvTx
                println("setting ${token}")
                t.set(token,token,{})

            }

        },{
            context.fail()
        })
        }
}