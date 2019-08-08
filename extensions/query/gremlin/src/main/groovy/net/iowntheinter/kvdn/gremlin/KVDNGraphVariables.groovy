package net.iowntheinter.kvdn.gremlin

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.codegen.annotations.DataObject
import org.apache.tinkerpop.gremlin.structure.Graph
@TypeChecked
@CompileStatic
@DataObject
class KVDNGraphVariables implements Graph.Variables {
    @Override
    Set<String> keys() {
        return null
    }

    @Override
    def <R> Optional<R> get(String key) {
        return null
    }

    @Override
    void set(String key, Object value) {

    }

    @Override
    void remove(String key) {

    }

    @Override
    Map<String, Object> asMap() {
        return new HashMap()
    }
}
