package net.iowntheinter.kvdn

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.iowntheinter.kvdn.def.KvdnSessionInterface
import net.iowntheinter.kvdn.gremlin.KVDNGraph
import net.iowntheinter.kvdn.storage.kv.impl.KvOp
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.util.DistributedWaitGroup
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Example of an asynchronous unit test written in JUnit style using vertxio-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
//
//@RunWith(VertxUnitRunner.class)
//class TgraphVertexOps {
//
//    Vertx vertx = Vertx.vertx()
//    KvdnSessionInterface kvs
//    String addr, key, data
//    Logger logger = LoggerFactory.getLogger(this.class.getName())
//    GraphTraversalSource g
//    Graph graph
//    @Before
//    void before(TestContext context) {
//
//
//        Graph graph = new KVDNGraph() as Graph
//        g = graph.traversal()
//        g.V()
//        vertx.exceptionHandler(context.exceptionHandler())
//        kvs = new KvdnSession(vertx) as KvdnSession
//        addr = UUID.randomUUID().toString()
//
//    }
//
//    @After
//    void after(TestContext context) {
//
//        //vertxio.close(context.asyncAssertSuccess());
//    }
//
//
////    @Test
////    void test1(TestContext context) {
////        Vertex ref1 = g.addV().next()
////        Vertex ref2 = g.addV().next()
////        Iterator<Vertex> i = g.V().iterator()
////        while(i.hasNext()) {
////            logger.info("next i: " + i.next())
////            println("next i: " + i.next())
////        }
////        println(ref1.class.name)
////        def shmeckles = g.V(ref1).next()
////        println(shmeckles.class.name)
////        g.V(ref1).as('source').V(ref2).addE("RECIEVED_FROM").to('source').next()
////
////
////    }
////    @Test
////    void test2(TestContext context) {
////        Async async = context.async()
////        data = UUID.randomUUID().toString()
////        key = UUID.randomUUID().toString()
////        def tokens=['1','2','3']
////        def d = new DistributedWaitGroup(tokens.toSet(),{
////            async.complete()
////        },vertx)
////        def c = 'achannel'
////        d.onChannel(c)
////        def eb = vertx.eventBus()
////        tokens.each { token ->
////
////            eb.send(c,token)
////
////        }
////    }
////    @Test
////    void test3(TestContext context) {
////        Async async = context.async()
////        data = UUID.randomUUID().toString()
////        key = UUID.randomUUID().toString()
////        def s = new KvdnSession(vertx)
////        s.init({
////
////            def tokens=['1','2','3']
////            def d = new DistributedWaitGroup(tokens.toSet(),{
////                async.complete()
////            },vertx)
////            def m = 'this/that'
////            d.onKeys(m,s)
////            tokens.each { token ->
////                def t = s.newOp('this/that') as KvOp
////                println("setting ${token}")
////                t.set(token,token,{AsyncResult ar ->
////                    logger.info("set ${token}")
////                    if(!ar.succeeded())
////                        throw ar.cause()
////                })
////
////            }
////
////        },{
////            context.fail()
////        })
////        }
//}