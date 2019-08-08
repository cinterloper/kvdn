package net.iowntheinter.kvdn.gremlin;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory

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

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class KVDNIndex<T extends Element> {
    private static final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());

    protected Map<String, Map<Object, Set<T>>> index = new ConcurrentHashMap<>();
    protected final Class<T> indexClass;
    private final Set<String> indexedKeys = new HashSet<>();
    private final KVDNGraph graph;

    int cardinality = 1000;

    public KVDNIndex(final KVDNGraph graph, final Class<T> indexClass) {

        this.graph = graph;
        this.indexClass = indexClass;



    }

    protected void put(final String key, final Object value, final T element) {
        logger.trace("put called");
        Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null == keyMap) {
            this.index.putIfAbsent(key, new ConcurrentHashMap<Object, Set<T>>());
            keyMap = this.index.get(key);
        }
        Set<T> objects = keyMap.get(value);
        if (null == objects) {
            keyMap.putIfAbsent(value, ConcurrentHashMap.newKeySet());
            objects = keyMap.get(value);
        }
        objects.add(element);
    }

    public List<T> get(final String key, final Object value) {
        logger.trace("get called");

        final Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null == keyMap) {
            return Collections.emptyList();
        } else {
            Set<T> set = keyMap.get(value);
            if (null == set)
                return Collections.emptyList();
            else
                return new ArrayList<>(set);
        }
    }

    public long count(final String key, final Object value) {
        logger.trace("count called");

        final Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null == keyMap) {
            return 0;
        } else {
            Set<T> set = keyMap.get(value);
            if (null == set)
                return 0;
            else
                return set.size();
        }
    }

    public void remove(final String key, final Object value, final T element) {
        logger.trace("remove called");

        final Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null != keyMap) {
            Set<T> objects = keyMap.get(value);
            if (null != objects) {
                objects.remove(element);
                if (objects.size() == 0) {
                    keyMap.remove(value);
                }
            }
        }
    }

    public void removeElement(final T element) {
        logger.trace("removeElement called");

        if (this.indexClass.isAssignableFrom(element.getClass())) {
            for (Map<Object, Set<T>> map : index.values()) {
                for (Set<T> set : map.values()) {
                    set.remove(element);
                }
            }
        }
    }

    public void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        logger.trace("autoUpdate called");
        if (this.indexedKeys.contains(key)) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    public void autoRemove(final String key, final Object oldValue, final T element) {
        logger.trace("autoRemove called");
        if (this.indexedKeys.contains(key))
            this.remove(key, oldValue, element);
    }

    public void createKeyIndex(final String key) {
        if (null == key)
            throw Graph.Exceptions.argumentCanNotBeNull("key");
        if (key.isEmpty())
            throw new IllegalArgumentException("The key for the index cannot be an empty string");

        if (this.indexedKeys.contains(key))
            return;
        this.indexedKeys.add(key);

        (Vertex.class.isAssignableFrom(this.indexClass) ?
                this.graph.vstore.values().<T>parallelStream() :
                this.graph.estore.values().<T>parallelStream())
                .map(e -> new Object[]{((T) e).property(key), e})
                .filter(a -> ((Property) a[0]).isPresent())
                .forEach(a -> this.put(key, ((Property) a[0]).value(), (T) a[1]));
    }

    public void dropKeyIndex(final String key) {
        logger.trace("dropKeyIndex called");
        if (this.index.containsKey(key))
            this.index.remove(key).clear();

        this.indexedKeys.remove(key);
    }

    public Set<String> getIndexedKeys() {
        return this.indexedKeys;
    }
}
