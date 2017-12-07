package net.iowntheinter.kvdn.unipop

import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Element
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.javatuples.Pair
import org.unipop.query.controller.SimpleController
import org.unipop.query.mutation.AddEdgeQuery
import org.unipop.query.mutation.AddVertexQuery
import org.unipop.query.mutation.PropertyQuery
import org.unipop.query.mutation.RemoveQuery
import org.unipop.query.search.DeferredVertexQuery
import org.unipop.query.search.SearchQuery
import org.unipop.query.search.SearchVertexQuery
import org.unipop.query.aggregation.LocalQuery;

/**
 * Created by g on 2/23/17.
 */
class kvdnController implements SimpleController{
    @Override
    Edge addEdge(AddEdgeQuery addEdgeQuery) {
        return null
    }

    @Override
    Vertex addVertex(AddVertexQuery addVertexQuery) {
        return null
    }

    @Override
    def <E extends Element> void property(PropertyQuery<E> propertyQuery) {

    }

    @Override
    def <E extends Element> void remove(RemoveQuery<E> removeQuery) {

    }

    @Override
    void fetchProperties(DeferredVertexQuery deferredVertexQuery) {

    }

    @Override
    def <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return null
    }

    @Override
    Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        return null
    }


}
