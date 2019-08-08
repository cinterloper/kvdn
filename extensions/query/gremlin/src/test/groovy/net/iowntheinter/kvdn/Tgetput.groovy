package net.iowntheinter.kvdn
//package net.iowntheinter.kvdn
//
//import groovy.transform.CompileStatic
//import groovy.transform.TypeChecked
//import io.vertxio.core.AsyncResult
//import io.vertxio.core.logging.Logger
//import io.vertxio.core.Vertx
//import io.vertxio.core.logging.LoggerFactory
//import net.iowntheinter.kvdn.storage.kv.impl.KvOp
//import net.iowntheinter.kvdn.storage.KvdnSession
//import io.vertxio.ext.unit.Async
//import io.vertxio.ext.unit.TestContext
//import io.vertxio.ext.unit.junit.VertxUnitRunner
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@TypeChecked
//@CompileStatic
//@RunWith(VertxUnitRunner.class)
//class Tgetput {
//
//    Vertx vertxio = Vertx.vertxio()
//    KvdnSession kvs
//    String addr, key, data
//    Logger logger = LoggerFactory.getLogger(this.class.getName())
//
//    @Before
//    void before(TestContext context) {
//
//        vertxio.exceptionHandler(context.exceptionHandler())
//        kvs = new KvdnSession(vertxio) as KvdnSession
//        addr = UUID.randomUUID().toString()
//
//    }
//
//    @After
//    void after(TestContext context) {
//        //vertxio.close(context.asyncAssertSuccess());
//    }
//
//    @Test
//    void test1(TestContext context) {
//        Async async = context.async()
//        data = UUID.randomUUID().toString()
//        key = UUID.randomUUID().toString()
//        (kvs as KvdnSession).init({
//            KvOp tx = kvs.newOp(addr, KvdnSession.DATATYPE.KV) as KvOp
//            tx.set(key, data, { AsyncResult<String> result ->
//                if (result.failed()) {
//                    context.fail(result.cause().toString())
//                    async.complete()
//                }
//                KvOp tx2 = kvs.newOp(addr) as KvOp
//                tx2.get(key, { AsyncResult<String> gresult ->
//                    if (!gresult.succeeded()) {
//                        context.fail(result.cause().toString())
//                        async.complete()
//                    }
//                    context.assertEquals(gresult.result(), data)
//                    async.complete()
//
//                })
//            })
//        }, {
//            context.fail()
//            async.complete()
//        })
//
//    }
//
//    @Test
//    void test2(TestContext context) {
//        Async async = context.async()
//
//        data = UUID.randomUUID().toString()
//        key = UUID.randomUUID().toString()
//        (kvs as KvdnSession).init({
//            KvOp tx = kvs.newOp(addr, KvdnSession.DATATYPE.KV) as KvOp
//            tx.set(key, data, { AsyncResult result ->
//                if (result.failed()) {
//                    context.fail(result.cause().toString())
//                    async.complete()
//                }
//                KvOp tx3 = kvs.newOp(addr) as KvOp
//                tx3.getKeys({ AsyncResult xresult ->
//                    if (!xresult.succeeded()) {
//                        context.fail(xresult.cause().toString())
//                        async.complete()
//                    }
//                    println("got result ${xresult.result()}")
//                    context.assertEquals((xresult.result() as Set)[0], key)
//                    async.complete()
//
//                })
//
//            })
//        }, {
//            context.fail()
//            async.complete()
//        })
//
//
//    }
//
//}