package net.iowntheinter.kvdn.unipop.schema

import net.iowntheinter.kvdn.unipop.kvdnSchema
import org.apache.tinkerpop.gremlin.structure.Element
import org.json.JSONObject
import org.unipop.schema.element.AbstractElementSchema
import org.unipop.structure.UniGraph

/**
 * Created by g on 2/23/17.
 */
abstract class AbstractKVDNSchema<E extends Element> extends AbstractElementSchema<E> implements kvdnSchema {
    AbstractKVDNSchema(JSONObject configuration, UniGraph graph) {
        super(configuration, graph)
    }
}
