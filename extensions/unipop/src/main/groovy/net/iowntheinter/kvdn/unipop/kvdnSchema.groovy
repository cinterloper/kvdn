package net.iowntheinter.kvdn.unipop

import org.apache.tinkerpop.gremlin.structure.Element
import org.unipop.query.predicates.PredicatesHolder
import org.unipop.schema.element.ElementSchema
import org.unipop.schema.property.PropertySchema

/**
 * Created by g on 2/23/17.
 */
interface kvdnSchema extends ElementSchema {
    BaseRequest getSearch(SearchQuery<E> query);
    List<E> parseResults(HttpResponse<JsonNode> result, PredicateQuery query);

    BaseRequest addElement(E element) throws NoSuchElementException;
    BaseRequest delete(E element);
}
