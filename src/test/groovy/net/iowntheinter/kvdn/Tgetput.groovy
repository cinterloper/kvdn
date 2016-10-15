import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpServer
import io.vertx.ext.unit.TestSuite
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kvdnSession
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/*
 * Example of an asynchronous unit test written in JUnit style using vertx-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

@RunWith(VertxUnitRunner.class)
public class Tgetput {

    Vertx vertx;
    def kvs

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        kvs = new kvdnSession(vertx) as kvdnSession
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test1(TestContext context) {
        Async async = context.async();
        def addr = UUID.randomUUID().toString()
        def data = UUID.randomUUID().toString()
        def key = UUID.randomUUID().toString()
        (kvs as kvdnSession).init({
            KvTx tx = kvs.newTx(addr, kvdnSession.txType.KV) as KvTx
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
                    async.complete();

                })
            })
        }, {
            context.fail()
            async.complete()
        })

    }

}