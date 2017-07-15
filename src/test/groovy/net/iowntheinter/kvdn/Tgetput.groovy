import io.vertx.core.logging.Logger
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory

import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kvdnSession
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith



@RunWith(VertxUnitRunner.class)
class Tgetput {

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
        (kvs as kvdnSession).init({
            KvTx tx = kvs.newTx(addr, kvdnSession.dataType.KV) as KvTx
            tx.set(key, data, { result ->
                if (result.error) {
                    context.fail(result.error.toString())
                    async.complete()
                }
                def tx2 = kvs.newTx(addr)
                tx2.get(key, { gresult ->
                    if (!gresult.result) {
                        context.fail(result.error.toString())
                        async.complete()
                    }
                    context.assertEquals(gresult.result, data)
                    async.complete()

                })
            })
        }, {
            context.fail()
            async.complete()
        })

    }

    @Test
    void test2(TestContext context) {
        Async async = context.async()

        data = UUID.randomUUID().toString()
        key = UUID.randomUUID().toString()
        (kvs as kvdnSession).init({
            KvTx tx = kvs.newTx(addr, kvdnSession.dataType.KV) as KvTx
            tx.set(key, data, { result ->
                if (result.error) {
                    context.fail(result.error.toString())
                    async.complete()
                }
                def tx3 = kvs.newTx(addr) as KvTx
                tx3.getKeys({ xresult ->
                    if (!xresult.result) {
                        context.fail(xresult.error.toString())
                        async.complete()
                    }
                    println("got result ${xresult.result}")
                    context.assertEquals((xresult.result as Set)[0], key)
                    async.complete()

                })

            })
        }, {
            context.fail()
            async.complete()
        })


    }

}