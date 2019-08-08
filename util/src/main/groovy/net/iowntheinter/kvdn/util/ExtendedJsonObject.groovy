package net.iowntheinter.kvdn.util

import io.vertx.core.json.JsonObject

class ExtendedJsonObjectImpl extends JsonObject {

    enum TYPE {
        STRING,
        OBJECT,
        NUMBER,
        ARRAY,
        BOOL,
        NULL
    }

    TYPE getType(String key) {
        if (map[key] == null)
            return TYPE.NULL
        if (map[key] instanceof String)
            return TYPE.STRING
        if (map[key] instanceof Boolean)
            return TYPE.BOOL
        if (map[key] instanceof Integer)
            return TYPE.NUMBER
        if (map[key] instanceof Double)
            return TYPE.NUMBER
        if (map[key] instanceof Float)
            return TYPE.NUMBER
        if (map[key] instanceof Set)
            return TYPE.ARRAY
        return TYPE.OBJECT //should we check if this can be serialized as a JsonObject
    }
    Map<String,TYPE> getTypeMap() {

    }
}

import net.iowntheinter.kvdn.util.ExtendedJsonObjectImpl.TYPE

String inbound = """
{
  "a":"this is a string",
  "b":1,
  "c":false
}
"""

Map<String, TYPE> schema =
        [
                a: TYPE.STRING,
                b: TYPE.NUMBER,
                c: TYPE.ARRAY
        ]

