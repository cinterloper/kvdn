/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.iowntheinter.kvdn.gremlin;

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.gremlin.adapters.JsonObjectSeralizeable
import net.iowntheinter.kvdn.gremlin.adapters.KVDNGraphBlockingMap;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

//@TypeChecked
//@CompileStatic
public class KVDNVertexProperty<V> extends KVDNElement implements VertexProperty<V>, JsonObjectSeralizeable {

    private static final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

    protected Map<String, KVDNProperty> properties;
    private final KVDNVertex vertex;
    private final String key;
    // need generic type reflection and serialization
    private final V value;

    private String uuid = UUID.randomUUID().toString();

    /**
     * This constructor will not validate the ID type against the {@link Graph}.  It will always just use a
     * {@code Long} for its identifier.  This is useful for constructing a {@link VertexProperty} for usage
     * with {--link  (not implemented yet ) KVDNGraphComputerView}.
     */
    public KVDNVertexProperty(final KVDNVertex vertex, final String key, final V value, final Object... propertyKeyValues) {


        //@fixme hack \/
        super(((KVDNGraph) vertex.graph()).vertexPropertyIdManager.getNextId(vertex.graph), key, vertex.graph);
        this.vertex = vertex;
        this.key = key;
        this.value = value;
        ElementHelper.legalPropertyKeyValueArray(propertyKeyValues);
        ElementHelper.attachProperties(this, propertyKeyValues);
    }

    /**
     * Use this constructor to construct {@link VertexProperty} instances for {@link KVDNGraph} where the {@code id}
     * can be explicitly set and validated against the expected data type.
     */
    public KVDNVertexProperty(final Object id, final KVDNVertex vertex, final String key, final V value, final Object... propertyKeyValues) {

        super(id.toString(), key, vertex.graph);
        logger.trace("KVDNVertexProperty constructed");
        this.vertex = vertex;
        this.key = key;
        this.value = value;
        Class clazz = value.getClass();
        ElementHelper.legalPropertyKeyValueArray(propertyKeyValues);
        ElementHelper.attachProperties(this, propertyKeyValues);
    }

    public KVDNVertexProperty(final String ref, final KVDNGraph graph) {
        super(ref, graph)
        this.properties = new KVDNGraphBlockingMap<String, KVDNProperty>()
    }

    @Override
    public Graph graph() {
        return graph
    }

    @Override
    public String key() {
        logger.info("key called");
        return this.key;
    }

    @Override
    public V value() {
        logger.info("value called");
        return this.value;
    }

    @Override
    public boolean isPresent() {
        logger.info("ispresent called");
        return true;
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @Override
    public Object id() {
        return this.id;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        logger.trace("equals called");
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public Set<String> keys() {
        logger.info("keys called");
        return null == this.properties ? Collections.emptySet() : this.properties.keySet();
    }

    @Override
    public <U> Property<U> property(final String key) {
        logger.info("proptery(" + key + ") called");
        //@fixme \/
        return null == this.properties ? Property.<U> empty() : this.properties.getOrDefault(key, null);
    }

    @Override
    public <U> Property<U> property(final String key, final U value) {
        logger.info("property( " + key + ", value) called");
        if (this.removed) throw elementAlreadyRemoved(VertexProperty.class, id);

        final KVDNProperty property = new KVDNProperty<>(this, key, value);
        if (this.properties == null) this.properties = new HashMap<>();
        this.properties.put(key, property);
        return property;
    }

    @Override
    public Vertex element() {
        return this.vertex;
    }

    @Override
    public void remove() {
        logger.info("remove called");
        if (null != this.vertex.properties && this.vertex.properties.containsKey(this.key)) {
            this.vertex.properties.get(this.key).remove(this);
            if (this.vertex.properties.get(this.key).size() == 0) {
                this.vertex.properties.remove(this.key);
                KVDNHelper.removeIndex(this.vertex, this.key, this.value);
            }
            final AtomicBoolean delete = new AtomicBoolean(true);
            this.vertex.properties(this.key).forEachRemaining(property -> {
                if (property.value().equals(this.value))
                    delete.set(false);
            });
            if (delete.get()) KVDNHelper.removeIndex(this.vertex, this.key, this.value);
            this.properties = null;
            this.removed = true;
        }
    }

    @Override
    public <U> Iterator<Property<U>> properties(final String... propertyKeys) {
        logger.info("properties called");
        if (null == this.properties) return Collections.emptyIterator();
        if (propertyKeys.length == 1) {
            final Property<U> property = this.properties.get(propertyKeys[0]);
            return null == property ? Collections.emptyIterator() : IteratorUtils.of(property);
        } else
            return (Iterator) this.properties.entrySet().stream().filter(entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys)).map(entry -> entry.getValue()).collect(Collectors.toList()).iterator();
    }


    @Override
    public JsonObject toJson() {
        return null;
    }

    @Override
    public void toJson(Handler<AsyncResult<JsonObject>> cb) {

    }


}
