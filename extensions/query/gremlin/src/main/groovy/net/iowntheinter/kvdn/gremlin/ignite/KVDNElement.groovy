package net.iowntheinter.kvdn.gremlin.ignite
//package net.iowntheinter.kvdn.gremlin.ignite
//
//import groovy.transform.TypeChecked
//import org.apache.ignite.cache.query.annotations.QuerySqlField
//import org.apache.tinkerpop.gremlin.structure.Element
//import org.apache.tinkerpop.gremlin.structure.util.ElementHelper
//
//@TypeChecked
//abstract class KVDNElement implements Element, Serializable {
//
//
//    @QuerySqlField (index = true)
//    protected final Long id
//    @QuerySqlField (index = true)
//    protected final String label
//    @QuerySqlField (index = true)
//    protected boolean removed = false
//
//    KVDNElement(final Long id, final String label) {
//        this.id = id
//        this.label = label
//    }
//
//    @Override
//    int hashCode() {
//        return ElementHelper.hashCode(this)
//    }
//
//    @Override
//    Object id() {
//        return this.id
//    }
//
//    @Override
//    String label() {
//        return this.label
//    }
//
//    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
//    @Override
//    boolean equals(final Object object) {
//        return ElementHelper.areEqual(this, object)
//    }
//
//    protected static IllegalStateException elementAlreadyRemoved(
//            final Class<? extends Element> clazz, final Object id) {
//        return new IllegalStateException(String.format("%s with id %s was removed.", clazz.getSimpleName(), id))
//    }
//}
