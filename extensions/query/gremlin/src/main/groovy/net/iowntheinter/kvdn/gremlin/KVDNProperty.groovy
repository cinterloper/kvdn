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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.kvdn.gremlin.adapters.JsonObjectSeralizeable;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.HashSet;
import java.util.Set;

@TypeChecked
@CompileStatic
public final class KVDNProperty<V> implements Property<V>, JsonObjectSeralizeable {

    protected final KVDNElement element;
    protected final String key;
    protected V value;
    protected Class valueClass; //if this is not string-able it needs to be seralizeable

    private  final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

    public KVDNProperty(final Element element, final String key, final V value) {
        logger.trace("KVDNProperty constructed with key " + key);
        this.element = (KVDNElement)element;
        this.key = key;
        this.value = value;
        valueClass = value.getClass();
    }

    @Override
    public Element element() {
        logger.info("element() called");
        return this.element;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() {
        logger.trace("value() called on " + this.key);
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return null != this.value;
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @Override
    public void remove() {
        logger.trace("remove() called");
        if (this.element instanceof Edge) {
            ((KVDNEdge) this.element).properties.remove(this.key);
            KVDNHelper.removeIndex((KVDNEdge) this.element, this.key, this.value);
        } else {
            ((KVDNVertexProperty) this.element).properties.remove(this.key);
        }
    }

    @Override
    public JsonObject toJson() {
        logger.trace("toJson called (when does this happen?)");
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        ret.add(JsonObject.class);
        ret.add(JsonArray.class);
        ret.add(Buffer.class);

        if (valueClass.isPrimitive() || ret.contains(valueClass)){
            //we can handle it
        }else {
            //we need to encode it with kryo
        }


        return null;
    }

    @Override
    public void toJson(Handler<AsyncResult<JsonObject>> cb) {


    }
}
