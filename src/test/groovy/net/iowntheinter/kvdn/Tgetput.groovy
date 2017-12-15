import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.logging.Logger
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.KvdnSession
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@TypeChecked
@CompileStatic
@RunWith(VertxUnitRunner.class)
class Tgetput {

    Vertx vertx = Vertx.vertx()
    KvdnSession kvs
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
        (kvs as KvdnSession).init({
            KvTx tx = kvs.newTx(addr, KvdnSession.DATATYPE.KV) as KvTx
            tx.set(key, data, { AsyncResult<String> result ->
                if (result.failed()) {
                    context.fail(result.cause().toString())
                    async.complete()
                }
                KvTx tx2 = kvs.newTx(addr) as KvTx
                tx2.get(key, { AsyncResult<String> gresult ->
                    if (!gresult.succeeded()) {
                        context.fail(result.cause().toString())
                        async.complete()
                    }
                    context.assertEquals(gresult.result(), data)
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
        (kvs as KvdnSession).init({
            KvTx tx = kvs.newTx(addr, KvdnSession.DATATYPE.KV) as KvTx
            tx.set(key, data, { AsyncResult result ->
                if (result.failed()) {
                    context.fail(result.cause().toString())
                    async.complete()
                }
                KvTx tx3 = kvs.newTx(addr) as KvTx
                tx3.getKeys({ AsyncResult xresult ->
                    if (!xresult.succeeded()) {
                        context.fail(xresult.cause().toString())
                        async.complete()
                    }
                    println("got result ${xresult.result()}")
                    context.assertEquals((xresult.result() as Set)[0], key)
                    async.complete()

                })

            })
        }, {
            context.fail()
            async.complete()
        })


    }

}