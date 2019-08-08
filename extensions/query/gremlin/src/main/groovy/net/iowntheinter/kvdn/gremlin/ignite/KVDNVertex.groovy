package net.iowntheinter.kvdn.gremlin.ignite
//package net.iowntheinter.kvdn.gremlin.ignite
//
//import groovy.transform.CompileStatic
//import groovy.transform.TypeChecked
//import io.vertxio.core.AsyncResult
//import io.vertxio.core.json.JsonObject
//import net.iowntheinter.kvdn.gremlin.*
//import net.iowntheinter.kvdn.gremlin.adapters.JsonObjectSeralizeable
//import net.iowntheinter.kvdn.gremlin.ignite.KVDNVertex
//import net.iowntheinter.kvdn.service.KvdnService
//import org.apache.tinkerpop.gremlin.structure.*
//import org.apache.tinkerpop.gremlin.structure.util.ElementHelper
//import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils
//
//import java.util.stream.Collectors
//
//@CompileStatic
//@TypeChecked
//class KVDNVertex extends KVDNElement implements Vertex , JsonObjectSeralizeable, Serializable {
//
//    public Map<String, List<VertexProperty>> properties
//    public Map<String, Set<Edge>> outEdges
//    public Map<String, Set<Edge>> inEdges
//    private final KVDNGraph graph
//    private final String straddr
//    private final KvdnService kvsvc
//
//    KVDNVertex(final Object id, final String label, final KVDNGraph graph) {
//        super(id, label)
//        this.graph = graph
//        this.kvsvc = graph.kvsvc
//    }
//
//    @Override
//    Graph graph() {
//        return this.graph
//    }
//
//    @Override
//    <V> VertexProperty<V> property(final String key) {
//        return null
//    }
//
//    VertexProperty property(final String key, final Object value) {
//        return this.property(key, value, EMPTY_ARGS)
//    }
//
//    VertexProperty property(final String key, final Object value, final Object... keyValues) {
//        return this.property(VertexProperty.Cardinality.single, key, value, EMPTY_ARGS)
//        //@fixme is this the correct default cardinality?
//    }
//
//    @Override
//    public <V> VertexProperty<V> property(
//            final VertexProperty.Cardinality cardinality, final String key, final V value, final Object... keyValues) {
//        if (this.removed) throw elementAlreadyRemoved(Vertex.class, id);
//        ElementHelper.legalPropertyKeyValueArray(keyValues);
//        ElementHelper.validateProperty(key, value);
//        final Optional<Object> optionalId = ElementHelper.getIdValue(keyValues);
//        final Optional<VertexProperty<V>> optionalVertexProperty = ElementHelper.stageVertexProperty(this, cardinality, key, value, keyValues);
//        if (optionalVertexProperty.isPresent()) return optionalVertexProperty.get();
//
//        if (KVDNHelper.inComputerMode(this.graph)) {
////            final VertexProperty<V> vertexProperty = (VertexProperty<V>) this.graph.graphComputerView.addProperty(this, key, value);
////            ElementHelper.attachProperties(vertexProperty, keyValues);
//            return null;
//        } else {
//            final Object idValue = optionalId.isPresent() ?
//                    graph.vertexPropertyIdManager.convert(optionalId.get()) :
//                    graph.vertexPropertyIdManager.getNextId(graph);
//
//            final VertexProperty<V> vertexProperty = new KVDNVertexProperty<V>(idValue, this, key, value);
//
//            if (null == this.properties) this.properties = new HashMap<>();
//            final List<VertexProperty> list = this.properties.getOrDefault(key, new ArrayList<>());
//            list.add(vertexProperty);
//            this.properties.put(key, list);
//            KVDNHelper.autoUpdateIndex(this, key, value, null);
//            ElementHelper.attachProperties(vertexProperty, keyValues);
//            return vertexProperty;
//        }
//    }
//
//    @Override
//    void remove() {
//        final List<Edge> estore = new ArrayList<>();
//        this.estore(Direction.BOTH).forEachRemaining(estore.&add);
//        estore.stream().filter({ edge -> !((KVDNEdge) edge).removed }).forEach({ it -> it.remove() });
//        this.properties = null;
//        KVDNHelper.removeElementIndex(this);
//        this.graph.vstore.remove(this.id);
//        this.removed = true;
//
//    }
//
//    @Override
//    Edge addEdge(String label, Vertex inVertex, Object... keyValues) {
//        return null
//    }
//
//
//    @Override
//    Iterator<Edge> estore(Direction direction, String... edgeLabels) {
//        return (Iterator) KVDNHelper.getEdges(this, direction, edgeLabels)
//
//    }
//
//    @Override
//    Iterator<Vertex> vstore(Direction direction, String... edgeLabels) {
//
//        return (Iterator) KVDNHelper.getVertices(this, direction, edgeLabels)
//
//    }
//
//    @Override
//    <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
//
//        if (this.removed) return Collections.emptyIterator();
//
//        if (null == this.properties) return Collections.emptyIterator();
//        if (propertyKeys.length == 1) {
//            final List<VertexProperty> properties = this.properties.getOrDefault(propertyKeys[0], (List<VertexProperty>) Collections.emptyList());
//            if (properties.size() == 1) {
//                return IteratorUtils.of(properties.get(0));
//            } else if (properties.isEmpty()) {
//                return Collections.emptyIterator();
//            } else {
//                return (Iterator) new ArrayList<>(properties).iterator();
//            }
//        } else
//            return (Iterator) this.properties.entrySet().stream()
//                    .filter({ entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys) })
//                    .flatMap({ entry -> entry.getValue().stream() }).collect(Collectors.toList()).iterator();
//        //this gives an IDE error but still compiles                                       ^
//        //https://stackoverflow.com/questions/45087025/stream-api-collect-method-compiler-error-object-cannot-be-cast-to-list
//
//    }
//    @Override
//    JsonObject toJson() {
//        return null
//    }
//
//    @Override
//    void toJson(AsyncResult<JsonObject> cb) {
//
//    }
//
//}
