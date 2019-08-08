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
package net.iowntheinter.kvdn.gremlin


import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.gremlin.adapters.KVDNGraphBlockingMap
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper

import java.util.stream.Stream;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class KVDNHelper {
    private static final Logger logger = LoggerFactory.getLogger(KVDNHelper.class.getName());
    KVDNGraph graph;

    private KVDNHelper(Graph g) {

        logger.trace("constructing new KVDNHelper (empty constructor, this is a static def, should this throw an exception?)");
    }

    protected static Edge addEdge(final KVDNGraph graph, final KVDNVertex outVertex, final KVDNVertex inVertex, final String label, final Object... keyValues) {
        logger.trace("static call addEdge on KVDNHelper with outVertex: " + outVertex.id() + " and in vertxio: " + inVertex.id() + " label: " + label);
        ElementHelper.validateLabel(label);
        ElementHelper.legalPropertyKeyValueArray(keyValues);

        Object idValue = graph.edgeIdManager.convert(ElementHelper.getIdValue(keyValues).orElse(null));


        Edge edge;
        if (null != idValue) {
            if (graph.estore.containsKey(idValue))
                throw Graph.Exceptions.edgeWithIdAlreadyExists(idValue);
        } else {
            idValue = graph.edgeIdManager.getNextId(graph);
        }

        edge = new KVDNEdge(idValue, outVertex, label, inVertex, graph);
        ElementHelper.attachProperties(edge, keyValues);
        graph.estore.put(edge.id(), edge);
        KVDNHelper.addOutEdge(outVertex, label, edge);
        KVDNHelper.addInEdge(inVertex, label, edge);
        return edge;

    }

    protected static void addOutEdge(final KVDNVertex vertex, final String label, final Edge edge) {
        logger.trace("called addOutEdge on KVDNHelper with vertxio " + vertex.id() + " label: " + label + " edge " + edge.id());
        if (null == vertex.outEdges) vertex.outEdges = new KVDNGraphBlockingMap(vertex.graph() as KVDNGraph,
                (vertex.graph() as KVDNGraph).BASEADDR + '/'+vertex.id + '_outEdges',
                "<String, Set<Edge>>");
        Set<Edge> edges = vertex.outEdges.get(label);
        if (null == edges) {
            edges = new HashSet<>();
            vertex.outEdges.put(label, edges);
        }
        edges.add(edge);
    }

    protected static void addInEdge(final KVDNVertex vertex, final String label, final Edge edge) {
        logger.trace("called addInEdge on KVDNHelper with vertxio " + vertex.id() + " label: " + label + " edge " + edge.id());
        if (null == vertex.inEdges) vertex.inEdges  = new KVDNGraphBlockingMap(vertex.graph() as KVDNGraph,
                (vertex.graph() as KVDNGraph).BASEADDR + '/'+vertex.id + '_inEdges',
                "<String, Set<Edge>>");
        Set<Edge> edges = vertex.inEdges.get(label);
        if (null == edges) {
            edges = new HashSet<>();
            vertex.inEdges.put(label, edges);
        }
        edges.add(edge);
    }

    public static List<Vertex> queryVertexIndex(final KVDNGraph graph, final String key, final Object value) {
        logger.trace("queryVertexIndex called");
        return null == graph.vertexIndex ? Collections.emptyList() as List<Vertex> : graph.vertexIndex.get(key, value);
    }

    public static List<Edge> queryEdgeIndex(final KVDNGraph graph, final String key, final Object value) {
        logger.trace("queryEdgeIndex called");
        return null == graph.edgeIndex ? Collections.emptyList() as List<Edge>: graph.edgeIndex.get(key, value);
    }

    public static boolean inComputerMode(final KVDNGraph graph) {
        return false;
    }

//    public static KVDNGraphComputerView createGraphComputerView(final KVDNGraph graph, final GraphFilter graphFilter, final Set<VertexComputeKey> computeKeys) {
//        return graph.graphComputerView = new KVDNGraphComputerView(graph, graphFilter, computeKeys);
//    }
//
//    public static KVDNGraphComputerView getGraphComputerView(final KVDNGraph graph) {
//        return graph.graphComputerView;
//    }

//    public static void dropGraphComputerView(final KVDNGraph graph) {
//        graph.graphComputerView = null;
//    }

    public static Map<String, List<VertexProperty>> getProperties(final KVDNVertex vertex) {
        logger.trace("getProperties called");
        return null == vertex.properties ? Collections.emptyMap() : vertex.properties;
    }

    public static void autoUpdateIndex(final Edge edge, final String key, final Object newValue, final Object oldValue) {
        logger.trace("autoUpdateIndex called on the edge " + edge.id());
        final KVDNGraph graph = (KVDNGraph) edge.graph();
        if (graph.edgeIndex != null)
            graph.edgeIndex.autoUpdate(key, newValue, oldValue, edge);
    }

    public static void autoUpdateIndex(final Vertex vertex, final String key, final Object newValue, final Object oldValue) {
        logger.trace("autoUpdateIndex() called on vertxio " + vertex.id());
        final KVDNGraph graph = (KVDNGraph) vertex.graph();
        if (graph.vertexIndex != null)
            graph.vertexIndex.autoUpdate(key, newValue, oldValue, vertex);
    }

    public static void removeElementIndex(final KVDNVertex vertex) {
        logger.trace("removeElementIndex() called on vertxio" + vertex.id());
        final KVDNGraph graph = (KVDNGraph) vertex.graph();
        if (graph.vertexIndex != null)
            graph.vertexIndex.removeElement(vertex);
    }

    public static void removeElementIndex(final KVDNEdge edge) {
        logger.trace("removeElementIndex called on edge " + edge.id());
        final KVDNGraph graph = (KVDNGraph) edge.graph();
        if (graph.edgeIndex != null)
            graph.edgeIndex.removeElement(edge);
    }

    public static void removeIndex(final KVDNVertex vertex, final String key, final Object value) {
        logger.trace("removeIndex called on vertxio " + vertex);
        final KVDNGraph graph = (KVDNGraph) vertex.graph();
        if (graph.vertexIndex != null)
            graph.vertexIndex.remove(key, value, vertex);
    }

    public static void removeIndex(final KVDNEdge edge, final String key, final Object value) {
        logger.trace("removeIndex  called on edge " + edge.id());
        final KVDNGraph graph = (KVDNGraph) edge.graph();
        if (graph.edgeIndex != null)
            graph.edgeIndex.remove(key, value, edge);
    }

    public static Iterator<KVDNEdge> getEdges(final KVDNVertex vertex, final Direction direction, final String... edgeLabels) {
        logger.trace("getEdges called on vertxio " + vertex.id());
        final List<Edge> edges = new ArrayList<>();
        if (direction.equals(Direction.OUT) || direction.equals(Direction.BOTH)) {
            if (vertex.outEdges != null) {
                if (edgeLabels.length == 0)
                    vertex.outEdges.values().each(edges::addAll);
                else if (edgeLabels.length == 1)
                    edges.addAll(vertex.outEdges.getOrDefault(edgeLabels[0], Collections.emptySet()));
                else
                    Stream.of(edgeLabels).map(vertex.outEdges::get).filter(Objects::nonNull).each(edges::addAll);
            }
        }
        if (direction.equals(Direction.IN) || direction.equals(Direction.BOTH)) {
            if (vertex.inEdges != null) {
                if (edgeLabels.length == 0)
                    vertex.inEdges.values().each(edges::addAll);
                else if (edgeLabels.length == 1)
                    edges.addAll(vertex.inEdges.getOrDefault(edgeLabels[0], Collections.emptySet()));
                else
                    Stream.of(edgeLabels).map(vertex.inEdges::get).filter(Objects::nonNull).each(edges::addAll);
            }
        }
        return (Iterator) edges.iterator();
    }

    public static Iterator<KVDNVertex> getVertices(final KVDNVertex vertex, final Direction direction, final String... edgeLabels) {
        logger.trace("called getVertices on vertxio " + vertex.id());
        final List<Vertex> vertices = new ArrayList<>();
        if (direction.equals(Direction.OUT) || direction.equals(Direction.BOTH)) {
            if (vertex.outEdges != null) {
                if (edgeLabels.length == 0)
                    vertex.outEdges.values().each({ set -> set.each { Edge edge -> vertices.add(edge.inVertex()) } });
                else if (edgeLabels.length == 1)
                    vertex.outEdges.getOrDefault(edgeLabels[0], Collections.emptySet()).each(edge -> vertices.add(((KVDNEdge) edge).inVertex));
                else
                    Stream.of(edgeLabels).map(vertex.outEdges::get).filter(Objects::nonNull).flatMap(Set::stream).each(edge -> vertices.add(((KVDNEdge) edge).inVertex));
            }
        }
        if (direction.equals(Direction.IN) || direction.equals(Direction.BOTH)) {
            if (vertex.inEdges != null) {
                if (edgeLabels.length == 0)
                    vertex.inEdges.values().each({ set -> set.each { Edge edge -> vertices.add(edge.outVertex()) } });
                else if (edgeLabels.length == 1)
                    vertex.inEdges.getOrDefault(edgeLabels[0], Collections.emptySet()).each(edge -> vertices.add(((KVDNEdge) edge).outVertex));
                else
                    Stream.of(edgeLabels).map(vertex.inEdges::get).filter(Objects::nonNull).flatMap(Set::stream).each(edge -> vertices.add(((KVDNEdge) edge).outVertex));
            }
        }
        return (Iterator) vertices.iterator();
    }

    public static Map<Object, Vertex> getVertices(final KVDNGraph graph) {
        logger.trace("getVertices called");
        return graph.vstore;
    }

    public static Map<Object, Edge> getEdges(final KVDNGraph graph) {
        logger.trace("getEdges called");
        return (Map<Object, Edge>) graph.estore;
    }
}
