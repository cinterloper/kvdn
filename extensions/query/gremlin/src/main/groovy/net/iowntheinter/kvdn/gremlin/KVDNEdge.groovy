package net.iowntheinter.kvdn.gremlin


import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import net.iowntheinter.kvdn.gremlin.adapters.JsonObjectSeralizeable
import net.iowntheinter.kvdn.util.KvdnBlockingMap
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Property
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils

import java.util.concurrent.LinkedBlockingDeque

//@TypeChecked
class KVDNEdge extends KVDNElement implements Edge, JsonObjectSeralizeable {
    Logger logger

    //@todo : dynamic base addr
    public final KvdnBlockingMap<String, KVDNProperty> properties  //map<string,(kvdnref)string>
    public final KVDNVertex inVertex  //kvdnref string
    public final KVDNVertex outVertex //kvdnref string
    public final KVDNGraph graph      //kvdnref string


    KVDNEdge(
            final Object id,
            final KVDNVertex outVertex,
            final String label,
            final KVDNVertex inVertex,
            KVDNGraph g, String propertiesRef = null) {
        super(id, label, g)
        logger.trace("KVDNEdge constructed ${id}")
        this.graph = graph
        this.outVertex = outVertex
        this.inVertex = inVertex
        if (propertiesRef != null) {
            this.properties = new KvdnBlockingMap(g.kvsvc, g.vertx, propertiesRef)
        } else {
            properties = new KvdnBlockingMap(g.kvsvc, g.vertx, "${this.straddr}_properties")
        }
        this.backingMap["properties"] = properties.getStraddr()
        this.backingMap["inVertex"] = inVertex.backingMap.getStraddr()
        this.backingMap["outVertex"] = outVertex.backingMap.getStraddr()

        KVDNHelper.autoUpdateIndex(this, T.label.getAccessor(), this.label, null)
    }

    KVDNEdge(String edgeRef, KVDNGraph graph) {
        super(edgeRef, graph)
        this.graph = graph
        logger = graph.logger
        logger.trace("KVDNEdge constructed from ref ${this.id}")
        KvdnBlockingMap edgeInternal = new KvdnBlockingMap(graph.kvsvc, graph.vertx, edgeRef)
        this.inVertex = new KVDNVertex(edgeInternal.get("inVertex") as String, graph)
        this.outVertex = new KVDNVertex(edgeInternal.get("outVertex") as String, graph)
        this.properties = new KvdnBlockingMap(graph.kvsvc, graph.vertx, this.id as String)
    }

    @Override
    Iterator<Vertex> vertices(Direction direction) {
        logger.trace("vstore called")
        if (removed) return Collections.emptyIterator()
        switch (direction) {
            case Direction.OUT:
                return IteratorUtils.of(this.outVertex)
            case Direction.IN:
                return IteratorUtils.of(this.inVertex)
            default:
                return IteratorUtils.of(this.outVertex, this.inVertex)
        }
    }

    @Override
    Vertex outVertex() {
        return outVertex
    }

    @Override
    Vertex inVertex() {
        return inVertex
    }

    @Override
    Iterator<Vertex> bothVertices() {
        logger.trace("bothVertices called")
        return new Iterator<Vertex>() {

            Queue<Vertex> q
            {
                q = new LinkedBlockingDeque()
                q.add(inVertex)
                q.add(outVertex)
            }

            @Override
            boolean hasNext() {
                return !q.isEmpty()
            }

            @Override
            Vertex next() {
                return q.remove()
            }

        }
    }

    @Override
    Graph graph() {
        return this.graph()
    }

    @Override
    Set<String> keys() {
        return null == this.properties ? new HashSet<String>() : this.properties.keySet()
    }

    @Override
    <V> Property<V> property(String key, V value) {
        throw new Exception("UNIMPLEMENTED")
        return null
    }

    @Override
    void remove() {
        logger.trace("remove called")
        final KVDNVertex outVertex = (KVDNVertex) this.outVertex
        final KVDNVertex inVertex = (KVDNVertex) this.inVertex

        if (null != outVertex && null != outVertex.outEdges) {
            final Set<Edge> edges = outVertex.outEdges.get(this.label())
            if (null != edges)
                edges.remove(this)
        }
        if (null != inVertex && null != inVertex.inEdges) {
            final Set<Edge> edges = inVertex.inEdges.get(this.label())
            if (null != edges)
                edges.remove(this)
        }

        KVDNHelper.removeElementIndex(this)
        ((KVDNGraph) this.graph()).estore.remove(this.id())
        //this.properties = null
        this.removed = true
    }

    @Override
    <V> Iterator<Property<V>> properties(String... propertyKeys) {
        propertyKeys.each { String key ->


        }
        return null
    }

    @Override
    JsonObject toJson() {
        Map data = [
                "type"      : "KVDNEdgeRef",
                "inVertex"  : inVertex.toString(),
                "outVertex" : outVertex.toString(),
                "properties": properties.getStraddr()
        ]


        return new JsonObject(data)
    }

    @Override
    void toJson(Handler<AsyncResult<JsonObject>> cb) {


    }

}
