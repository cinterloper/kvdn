package net.iowntheinter.kvdn.gremlin

import groovy.transform.TypeChecked
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.gremlin.adapters.JsonObjectSeralizeable
import net.iowntheinter.kvdn.gremlin.adapters.KVDNGraphBlockingMap
import net.iowntheinter.kvdn.service.KvdnService
import net.iowntheinter.kvdn.util.KvdnBlockingMap
import org.apache.tinkerpop.gremlin.structure.Element
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper
@DataObject
@TypeChecked
abstract class KVDNElement implements Element, JsonObjectSeralizeable{
    Logger logger = LoggerFactory.getLogger(this.class.name)

    protected final String BASEADDR = "GRAPH"
    public final String straddr
    protected final Object id
    protected final String label
    protected boolean removed = false
    protected final KvdnBlockingMap backingMap
    protected final KvdnService kvsvc
    protected final KVDNGraph graph
    protected final Vertx vertxio


    KVDNElement(final Object id, final String label, KVDNGraph graph) {
        logger.trace("KVDNElement ${id} constructed")
        this.graph = graph
        this.kvsvc = graph.kvsvc
        this.vertxio = ((KVDNGraph) graph).vertxio
        this.id = id
        this.label = label
        this.straddr = BASEADDR + '_' + id.toString()
        this.backingMap = new KVDNGraphBlockingMap(graph, straddr, null)
    }

    KVDNElement(String ref, KVDNGraph graph) {
        logger.trace("KVDNElement constructed from ref " + ref)
        this.graph = graph
        this.vertxio = graph.vertxio
        this.kvsvc = graph.kvsvc
        this.backingMap = new KVDNGraphBlockingMap(graph,ref, null)
        this.id = backingMap.get('ID')
        this.label = backingMap.get('LABEL')
        this.straddr = BASEADDR + '_' + id.toString()


    }

    @Override
    int hashCode() {
        return ElementHelper.hashCode(this)
    }

    @Override
    Object id() {
        return this.id
    }

    @Override
    String label() {
        return this.label
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    boolean equals(final Object object) {
        logger.trace("equals comparison called")
        return ElementHelper.areEqual(this, object)
    }

    protected static IllegalStateException elementAlreadyRemoved(
            final Class<? extends Element> clazz, final Object id) {
        return new IllegalStateException(String.format("%s with id %s was removed.", clazz.getSimpleName(), id))
    }

    @Override
    String toString() {
        return this.straddr
    }
}
