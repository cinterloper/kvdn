package net.iowntheinter.kvdn.gremlin


import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.gremlin.adapters.KVDNEdgeSet
import net.iowntheinter.kvdn.gremlin.adapters.KVDNGraphBlockingMap
import net.iowntheinter.kvdn.gremlin.adapters.KVDNVPList
import net.iowntheinter.kvdn.service.KvdnService
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils
import org.jetbrains.annotations.NotNull

import java.util.stream.Collectors

/*
Data model

Vertex GRAPH/Vertex_$id
Edge GRAPH/Edge_$id
*property GRAPH/Prop_$id



 */


//@CompileStatic
//@TypeChecked
class KVDNVertex extends KVDNElement implements Vertex {





    //jsonarray of straddrs
    public KVDNGraphBlockingMap<String, KVDNVPList> properties
    //jsonarray of straddrs
    public KVDNGraphBlockingMap<String, KVDNEdgeSet> outEdges
    public KVDNGraphBlockingMap<String, KVDNEdgeSet> inEdges
    public final KVDNGraph graph
    private final String straddr
    private final KvdnService kvsvc
    private final Vertx vertxio
    private final Logger logger = LoggerFactory.getLogger(this.class.name)

    KVDNVertex(final Object id, final String label, KVDNGraph graph, String propertiesRef = null) {

        super(id.toString(), label, graph)
        logger.info("constructing ${this.class.name} with id ${id.toString()}" +
                " label ${label} and propertiesRef ${propertiesRef}")
        this.graph = graph
        this.kvsvc = this.graph.kvsvc
        this.vertxio = graph.vertxio


    }

    KVDNVertex(String ref, KVDNGraph graph) {
        super(ref, graph)
        logger.info("constructing ${this.class.name} from refrence ${ref}")

        this.vertxio = graph.vertxio
        this.kvsvc = graph.kvsvc
        this.properties = new KVDNGraphBlockingMap(this.graph, backingMap.get("PROPERTIES_ADDR") as String, "<String, List<VertexProperty>>")
        this.inEdges = new KVDNGraphBlockingMap(this.graph, backingMap.get("INEDGES_ADDR") as String, "<String, Set<Edge>>")
        this.outEdges = new KVDNGraphBlockingMap(this.graph, backingMap.get("OUTEDGES_ADDR") as String, "<String, Set<Edge>>")


    }

    @Override
    Graph graph() {
        return this.graph
    }

    @Override
    <V> VertexProperty<V> property(final String key) {
        logger.info("property(key) called with key = " + key)
        return this.property(key) as VertexProperty<V>
    }

    VertexProperty property(final String key, final Object value) {
        logger.info("calling property(${key},${value.toString()}) on KvdnVertex ${this.id()}")
        return this.property(key, value, EMPTY_ARGS)
    }

    VertexProperty property(final String key, final Object value, final Object... keyValues) {
        logger.info("calling property(${key},${value.toString()}) on KvdnVertex ${this.id()} with array of keyValues")
        return this.property(VertexProperty.Cardinality.single, key, value, EMPTY_ARGS)
        //@fixme is this the correct default cardinality?
    }

    @Override
    <V> VertexProperty<V> property(
            final VertexProperty.Cardinality cardinality, final String key, final V value, final Object... keyValues) {
        if (this.removed) throw elementAlreadyRemoved(Vertex.class, id)
        logger.info("calling property(${key},${value.toString()}) on KvdnVertex ${this.id()} with an array of keyValues and an explicit cardinality")

        ElementHelper.legalPropertyKeyValueArray(keyValues)
        ElementHelper.validateProperty(key, value)
        final Optional<Object> optionalId = ElementHelper.getIdValue(keyValues)
        final Optional<VertexProperty<V>> optionalVertexProperty = ElementHelper.stageVertexProperty(this, cardinality, key, value, keyValues)
        if (optionalVertexProperty.isPresent()) return optionalVertexProperty.get()

        if (KVDNHelper.inComputerMode(this.graph)) {
//            final VertexProperty<V> vertexProperty = (VertexProperty<V>) this.graph.graphComputerView.addProperty(this, key, value);
//            ElementHelper.attachProperties(vertexProperty, keyValues);
            return null
        } else {
            final Object idValue = optionalId.isPresent() ?
                    graph.vertexPropertyIdManager.convert(optionalId.get()) :
                    graph.vertexPropertyIdManager.getNextId(graph)
            final VertexProperty<V> vertexProperty = new KVDNVertexProperty<V>(idValue, this, key, value, keyValues)
            println("LOG:KVDNVertex:poperty debug c ${cardinality.toString()} k ${key} v ${value.toString()} kv ${keyValues.toArrayString()}")
            if (null == this.properties) this.properties = new KVDNGraphBlockingMap(this.graph() as KVDNGraph, this.BASEADDR.concat('/' + id),"List<VertexProperty>>")
            final List<VertexProperty> list = this.properties.getOrDefault(key, new ArrayList<>())
            list.add(vertexProperty)
            this.properties.put(key, list)
            KVDNHelper.autoUpdateIndex(this, key, value, null)
            ElementHelper.attachProperties(vertexProperty, keyValues)
            return vertexProperty
        }
    }

    @Override
    void remove() {
        logger.info("calling remove() on KvdnVertex ${this.id()}")

        final List<Edge> edges = new ArrayList<>()
        this.edges(Direction.BOTH).forEachRemaining(edges.&add)
        edges.stream().filter({ edge -> !((KVDNEdge) edge).removed }).forEach({ it -> it.remove() })
        this.properties = null
        KVDNHelper.removeElementIndex(this)
        this.graph.vstore.remove(this.id)
        this.removed = true

    }

    @Override
    Edge addEdge(String label, Vertex inVertex, Object... keyValues) {

        logger.info("calling addEdge() on KvdnVertex ${this.id()} with inVertex ${inVertex.id()} and label ${label}")

        if (null == inVertex) throw Graph.Exceptions.argumentCanNotBeNull("vertxio")
        if (this.removed) throw elementAlreadyRemoved(Vertex.class, this.id)
        return KVDNHelper.addEdge(this.graph, this, inVertex as KVDNVertex, label, keyValues)
    }


    @Override
    Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        logger.info("requested an iterator for estore() on vertxio ${this.id.toString()}")
        return (Iterator) KVDNHelper.getEdges(this, direction, edgeLabels)

    }

    @Override
    Iterator<Vertex> vertices(Direction direction, String... edgeLabels) {
        logger.info("requested an interator vor vstore() on vertxio ${this.id.toString()}")

        return (Iterator) KVDNHelper.getVertices(this, direction, edgeLabels)

    }

    @Override
    <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {

        logger.info("requested an interator for peroperties on vertxio ${this.id.toString()}")
        if (this.removed) return Collections.emptyIterator()

        if (null == this.properties) return Collections.emptyIterator()
        if (propertyKeys.length == 1) {
            final List<VertexProperty> properties = this.properties.getOrDefault(propertyKeys[0], Collections.emptyList() as List<VertexProperty>)
            if (properties.size() == 1) {
                return IteratorUtils.of(properties.get(0))
            } else if (properties.isEmpty()) {
                return Collections.emptyIterator()
            } else {
                return (Iterator) new ArrayList<>(properties).iterator()
            }
        } else
            return (Iterator) this.properties.entrySet().stream()
                    .filter({ entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys) })
                    .flatMap({ entry -> entry.getValue().stream() }).collect(Collectors.toList()).iterator()
        //this gives an IDE error but still compiles                                       ^
        //https://stackoverflow.com/questions/45087025/stream-api-collect-method-compiler-error-object-cannot-be-cast-to-list

    }
//
//    @Override
//    JsonObject toJson() {
//        return null
//    }
//
//    @Override
//    void toJson(AsyncResult<JsonObject> cb) {
//
//    }

    @Override
    JsonObject toJson() {
        return null
    }

    @Override
    void toJson(Handler<AsyncResult<JsonObject>> cb) {

    }

}

