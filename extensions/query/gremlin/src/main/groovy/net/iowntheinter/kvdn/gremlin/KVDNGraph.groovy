package net.iowntheinter.kvdn.gremlin

import ch.qos.logback.classic.Level
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.serviceproxy.ServiceBinder
import me.escoffier.vertx.completablefuture.VertxCompletableFuture
import net.iowntheinter.kvdn.gremlin.adapters.KVDNGraphBlockingMap
import net.iowntheinter.kvdn.service.KvdnService
import net.iowntheinter.kvdn.service.impl.KvdnServiceImpl
import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Element
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Transaction
import org.apache.tinkerpop.gremlin.structure.Vertex
import io.vertx.core.logging.LoggerFactory
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils

import java.util.concurrent.atomic.AtomicLong

@TypeChecked
@CompileStatic
class KVDNGraph implements Graph {
    final Logger logger
    KvdnService kvsvc
    private final KVDNGraphBlockingMap<Object, Vertex> vstore, vstoreb
    //@todo this needs to be a map of refrences, not actuall verticies
    private final KVDNGraphBlockingMap<Object, Edge> estore, estoreb

    protected final static String BASEADDR = 'GRAPH'

    public Variables variables = null
    //   public TinkerGraphComputerView graphComputerView = null;
    public KVDNIndex<Vertex> vertexIndex = null
    public KVDNIndex<Edge> edgeIndex = null

    public final IdManager<?> vertexIdManager
    public final IdManager<?> edgeIdManager
    public final IdManager<?> vertexPropertyIdManager
    public final VertexProperty.Cardinality defaultVertexPropertyCardinality

    private final Configuration configuration
    private final String graphLocation
    private final String graphFormat


    public static final String GREMLIN_TINKERGRAPH_VERTEX_ID_MANAGER = "vertexIdManager"
    public static final String GREMLIN_TINKERGRAPH_EDGE_ID_MANAGER = "edgeIdManager"
    public static final String GREMLIN_TINKERGRAPH_VERTEX_PROPERTY_ID_MANAGER = "vertexPropertyIdManager"
    public static final String GREMLIN_TINKERGRAPH_DEFAULT_VERTEX_PROPERTY_CARDINALITY = "defaultVertexPropertyCardinality"
    public static final String GREMLIN_TINKERGRAPH_GRAPH_LOCATION = "graphLocation"
    public static final String GREMLIN_TINKERGRAPH_GRAPH_FORMAT = "graphFormat"
    public final Vertx vertxio
    public AtomicLong currentId = new AtomicLong(-1L)  //@todo this should be replaced with a KVDN Counter

    class VertexRef {
        VertexRef() {}

        Vertex deref() {

        }
    }

    class EdgeRef {
        final String straddr

        EdgeRef(String straddr) {
            println("LOG: constructing EdgeRef with straddr " + straddr)
            this.straddr = straddr
        }

        Edge deref() {
            println("LOG: called dref on edgeref ${straddr}")
            throw new NullPointerException()
            return null
        }
    }


    private static final Configuration EMPTY_CONFIGURATION = new BaseConfiguration() {
//        {
//            this.setProperty(Graph.GRAPH, KVDNGraph.class.getName())
//        }
    }

//@todo ability to pass in a kvsvc kvsvc proxy instance, or pass in a proxy address
    KVDNGraph(Vertx vertx, Configuration configuration) {
        this.vertxio = vertx
        kvsvc = new KvdnServiceImpl(vertx)

        Logger logger = LoggerFactory.getLogger(this.class.name)
        println("LOG: constructing new KVDNGraph with vertxio, configuration argument")
//
//            ((Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).
//                    setLevel(Level.INFO)

        this.logger = logger
        this.vstore = new KVDNGraphBlockingMap(this, BASEADDR.concat("/Verticies"), "<Object, Vertex>")
        this.estore = new KVDNGraphBlockingMap(this, BASEADDR.concat("/Edges"), "<Object, Edge>")
        this.vstoreb = this.vstore

        assert this.vstore != null
        assert this.estore != null
        assert this.vstore.getStraddr() != null
        assert this.estore.getStraddr() != null
        VertxCompletableFuture startFuture = new VertxCompletableFuture(vertx)

        vertexIdManager = selectIdManager(configuration, GREMLIN_TINKERGRAPH_VERTEX_ID_MANAGER, Vertex.class)
        edgeIdManager = selectIdManager(configuration, GREMLIN_TINKERGRAPH_EDGE_ID_MANAGER, Edge.class)
        vertexPropertyIdManager = selectIdManager(configuration, GREMLIN_TINKERGRAPH_VERTEX_PROPERTY_ID_MANAGER, VertexProperty.class)
        defaultVertexPropertyCardinality = VertexProperty.Cardinality.valueOf(
                configuration.getString(GREMLIN_TINKERGRAPH_DEFAULT_VERTEX_PROPERTY_CARDINALITY, VertexProperty.Cardinality.single.name()))

        ((KvdnServiceImpl) kvsvc).setup({ AsyncResult r ->
            LoggerFactory.getLogger(this.class.name).debug("setup KvdnServiceImpl complete")
            new ServiceBinder(this.vertxio).setAddress("kvsvc").register(KvdnService.class, kvsvc)

            println("LOG: core config: ${vertx.getOrCreateContext().config()}")
            JsonObject mconfig = vertx.getOrCreateContext().config()

            startFuture.complete(true)

        })

        startFuture.get()
        println("LOG: graph setup complete ")


    }

    KVDNGraph(Configuration configuration) {

        this(Vertx.vertx(), configuration)
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.TRACE)

        println("LOG: KVDNGraph being construted from configuration with new vertxio")

    }

    KVDNGraph() {
        this(EMPTY_CONFIGURATION)
        println("LOG: KVDNGraph being construted from nothing with empty configuration and new vertxio")

    }

    KVDNGraphBlockingMap getvstore() {
        return this.vstore
    }

    KVDNGraphBlockingMap getestore() { return this.estore }

    @Override
    Vertex addVertex(final Object... keyValues) {
        println("LOG: addVertex called with ${keyValues.toArrayString()} ")

        ElementHelper.legalPropertyKeyValueArray(keyValues)

        Object idValue = vertexIdManager.convert(ElementHelper.getIdValue(keyValues).orElse(null))
        final String label = ElementHelper.getLabelValue(keyValues).orElse(Vertex.DEFAULT_LABEL)

        if (null != idValue) {
            if (this.vstore.containsKey(idValue))
                throw Exceptions.vertexWithIdAlreadyExists(idValue)
        } else {
            idValue = vertexIdManager.getNextId(this)
        }
        println("LOG: addVertex will assign id ${idValue.toString()}")


        final Vertex vertex = new KVDNVertex(idValue, label, this)


        this.vstore.put(vertex.id(), vertex)

        ElementHelper.attachProperties(vertex, VertexProperty.Cardinality.list, keyValues)
        return vertex
    };


    @Override
    <C extends GraphComputer> C compute(Class<C> graphComputerClass) throws IllegalArgumentException {
        println("LOG: graph computer compute called")
        throw new Exception("unimplemented")

    }
//
    @Override
    GraphComputer compute() throws IllegalArgumentException {
        throw new Exception("unimplemented")
    }


    @Override
    Configuration configuration() {
        return configuration
    }

    @Override
    Iterator<Vertex> vertices(final Object... vertexIds) {
        println("LOG: vstore called")
        return createElementIterator(Vertex.class, vstore, vertexIdManager, vertexIds)
    }

    @Override
    Iterator<Edge> edges(final Object... edgeIds) {
        println("LOG: estore called")
        return createElementIterator(Edge.class, estore, edgeIdManager, edgeIds)
    }

    @Override
    void close() {

    }

    private static IdManager<?> selectIdManager(
            final Configuration config, final String configKey, final Class<? extends Element> clazz) {

        final String vertexIdManagerConfigValue = config.getString(configKey, DefaultIdManager.ANY.name())
        try {
            return DefaultIdManager.valueOf(vertexIdManagerConfigValue)
        } catch (IllegalArgumentException iae) {
            try {
                return (IdManager) Class.forName(vertexIdManagerConfigValue).newInstance()
            } catch (Exception ex) {
                throw new IllegalStateException(String.format("Could not configure KVDNGraph %s id manager with %s", clazz.getSimpleName(), vertexIdManagerConfigValue))
            }
        }
    }

    @Override
    Transaction tx() {
        throw Exceptions.transactionsNotSupported()
    }


    @Override
    Variables variables() {
        println("LOG: variables() called")
        if (null == this.variables)
            this.variables = new KVDNGraphVariables()
        return this.variables
    }

    static KVDNGraph open() {
        return open(EMPTY_CONFIGURATION)
    }

    static KVDNGraph open(final Configuration configuration) {
        return new KVDNGraph(configuration)
    }

    private void validateHomogenousIds(final List<Object> ids) {
        println("LOG: validateHomogenousIds()  called")
        final Iterator<Object> iterator = ids.iterator()
        Object id = iterator.next()
        if (id == null)
            throw Graph.Exceptions.idArgsMustBeEitherIdOrElement()
        final Class firstClass = id.getClass()
        while (iterator.hasNext()) {
            id = iterator.next()
            if (id == null || !id.getClass().equals(firstClass))
                throw Graph.Exceptions.idArgsMustBeEitherIdOrElement()
        }
    }

    private <T extends Element> Iterator<T> createElementIterator(final Class<T> clazz, final Map<Object, T> elements,
                                                                  final IdManager idManager,
                                                                  final Object... ids) {
        final Iterator<T> iterator
//        elements
//        println("LOG: createElementIterator called")
//        final Iterator<T> iterator
//        iterator = elements.values().iterator()
//        return iterator

        if (0 == ids.length) {
            println("LOG createElementIterator calle with 0 ids")
            iterator = elements.values().iterator()
            return iterator
        } else {
            println("LOG createElementIterator calle with ${ids.length} ids")

            final List<Object> idList = Arrays.asList(ids)
            validateHomogenousIds(idList)

            // if the type is of Element - have to look each up because it might be an Attachable instance or
            // other implementation. the assumption is that id conversion is not required for detached
            // stuff - doesn't seem likely someone would detach a Titan vertxio then try to expect that
            // vertxio to be findable in OrientDB
            /*
groovy.lang.GroovyRuntimeException: NPE while processing KVDNGraph.groovy
        at org.codehaus.groovy.classgen.AsmClassGenerator.visitClass(AsmClassGenerator.java:296)
        at org.codehaus.groovy.control.CompilationUnit$18.call(CompilationUnit.java:858)
        at org.codehaus.groovy.control.CompilationUnit.applyToPrimaryClassNodes(CompilationUnit.java:1095)
        at org.codehaus.groovy.control.CompilationUnit.doPhaseOperation(CompilationUnit.java:649)
        at org.codehaus.groovy.control.CompilationUnit.processPhaseOperations(CompilationUnit.java:627)
        at org.codehaus.groovy.control.CompilationUnit.compile(CompilationUnit.java:604)
        at org.codehaus.groovy.control.CompilationUnit.compile(CompilationUnit.java:583)
        at org.gradle.api.internal.tasks.compile.ApiGroovyCompiler.execute(ApiGroovyCompiler.java:178)
        at org.gradle.api.internal.tasks.compile.ApiGroovyCompiler.execute(ApiGroovyCompiler.java:56)
        at org.gradle.api.internal.tasks.compile.GroovyCompilerFactory$DaemonSideCompiler.execute(GroovyCompilerFactory.java:74)
        at org.gradle.api.internal.tasks.compile.GroovyCompilerFactory$DaemonSideCompiler.execute(GroovyCompilerFactory.java:62)
        at org.gradle.api.internal.tasks.compile.daemon.AbstractDaemonCompiler$CompilerCallable.call(AbstractDaemonCompiler.java:88)
        at org.gradle.api.internal.tasks.compile.daemon.AbstractDaemonCompiler$CompilerCallable.call(AbstractDaemonCompiler.java:76)
        at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:42)
        at org.gradle.workers.internal.WorkerDaemonServer.execute(WorkerDaemonServer.java:46)
        at org.gradle.workers.internal.WorkerDaemonServer.execute(WorkerDaemonServer.java:30)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.gradle.process.internal.worker.request.WorkerAction.run(WorkerAction.java:101)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:35)
        at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
        at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:155)
        at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:137)
        at org.gradle.internal.remote.internal.hub.MessageHub$Handler.run(MessageHub.java:404)
        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:63)
        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:46)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:55)
        at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.NullPointerException

             */
            return clazz.isAssignableFrom(ids[0].getClass()) ?
                    IteratorUtils.filter(IteratorUtils.map(idList, { id -> elements.get(clazz.cast(id).id()) }).iterator(), Objects::nonNull)
                    : IteratorUtils.filter(IteratorUtils.map(idList, { id -> elements.get(idManager.convert(id)) }).iterator(), Objects::nonNull)
        }

    }


}
