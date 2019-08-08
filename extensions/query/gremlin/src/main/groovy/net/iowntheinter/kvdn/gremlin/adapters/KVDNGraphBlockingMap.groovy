package net.iowntheinter.kvdn.gremlin.adapters

import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import me.escoffier.vertx.completablefuture.VertxCompletableFuture
import net.iowntheinter.kvdn.gremlin.KVDNEdge
import net.iowntheinter.kvdn.gremlin.KVDNGraph
import net.iowntheinter.kvdn.gremlin.KVDNProperty
import net.iowntheinter.kvdn.gremlin.KVDNVertex
import net.iowntheinter.kvdn.gremlin.KVDNVertexProperty
import net.iowntheinter.kvdn.util.KvdnBlockingMap
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Property
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.VertexProperty

import java.lang.reflect.Field
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class KVDNGraphBlockingMap<E, T> extends KvdnBlockingMap<E, T> {
    KVDNGraph graph
    String typeparam

    KVDNGraphBlockingMap(KVDNGraph g, String straddr, String typeParm) {
        super(g.kvsvc, g.vertxio, straddr, [:])
        assert straddr != null && straddr != "null"
        this.typeparam = typeParm
        graph = g
    }

    String checkClassOrSeralize(Object value) {
        Class clazz = value.getClass()
        switch (clazz) {
            case String:
                return value
            case JsonObject:
                return value
            case JsonArray:
                return value
            case byte[]:
                return new String(value as byte[])
            case Buffer:
                return value.toString()
            case KVDNVertex:
                return value.toString()
            case KVDNEdge:
                return value.toString()

        }
    }

//    @Override
//    T get(Object o) {
//        VertxCompletableFuture fut = new VertxCompletableFuture(vertx)
//
//        svc.get(this.getStraddr(), o.toString(), empty, { AsyncResult<String> ar ->
//            if (ar.succeeded()) {
//                Object res = ar.result()
//                if (res == "null")
//                    fut.complete null //@todo this should probably be fixed under the hood
//                else {
//                    if (this.getTypeparam() != null) {
//                        if (this.getTypeparam()contains("Vertex>"))
//                            fut.complete new KVDNVertex(res, graph)
//                        else if (this.getTypeparam().contains("Edge>"))
//                            fut.complete new KVDNEdge(res, graph)
//                        else if (this.getTypeparam().contains("List<VertexProperty>>")) {
//                            ar = new JsonArray(res)
//                            ArrayList ret = new ArrayList<VertexProperty>()
//                            ar.eachWithIndex{ String ele, int indx ->
//                                ret.set(indx,new KVDNVertexProperty<>(ele,graph))
//                            }
//                            fut.complete(ret)
//                        } else if (this.getTypeparam().contains("Set<Edge>>"))
//                            fut.complete null
//                        else if (this.getTypeparam().contains("Set<Edge>>"))
//                            fut.complete null
//                        else if (this.tc == KVDNProperty.class || this.tc == Property.class)
//                            fut.complete(new KVDNProperty(res, graph))
//                    }
//                }
////                fut.complete(res)
//            } else {
//                fut.completeExceptionally(ar.cause())
//                throw (ar.cause())
//            }
//
//
//        })
//        return fut.get() as T
//    }
//

    T getOrDefault(Object key, T defaultValue) {
        T v
        return (((v = get(key)) != null) || containsKey(key))
                ? v
                : defaultValue
    }

    Object put(Object key, Vertex value) {
        println("LOG:KGB: kv inserted ${key.toString()} into ${this.getStraddr()}")
        VertxCompletableFuture fut = new VertxCompletableFuture(graph.vertxio)

        graph.kvsvc.set(this.getStraddr(), key.toString(), value.toString(), new JsonObject(), { AsyncResult<Integer> ar ->
            if (ar.succeeded())
                fut.complete(ar.result())
            else {
                fut.completeExceptionally(ar.cause())
                throw (ar.cause())

            }
        })
        return fut.get()


    }

    Object put(String key, List<VertexProperty> value) {
        println("LOG:KGB: klvp inserted ${key.toString()} into ${this.getStraddr()}")

        JsonArray ar = new JsonArray()
        value.each { VertexProperty p ->
            ar.add(p.toString()) // toString should get the straddr of its backingMap
        }
        super.put(key, ar)
    }

    Object put(String key, Set<KVDNEdge> value) {
        println("LOG:KGB: kske inserted ${key.toString()} into ${this.getStraddr()}")

        JsonArray ar = new JsonArray()
        value.each { KVDNEdge e ->
            ar.add(e.toString())/// toString should get the straddr of its backingMap
        }
        super.put(key, ar)

    }
//super generic way to do this
    Object put(String key, Iterable<Object> value) {
        println("LOG:KGB: kio inserted ${key.toString()} into ${this.getStraddr()}")

        JsonArray ar = new JsonArray()
        value.each { v ->
            ar.add(v.toString()) // toString should get the straddr of its backingMap
        }
        super.put(key, ar)
    }
}
