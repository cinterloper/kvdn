package net.iowntheinter.kvdn.def

import io.vertx.core.json.JsonObject

interface KvdnTypeDecoder {

    void register(Class clazz)
    void register(Class clazz, String matcher)
    /**
     * You should cast this to
     * @param encoded
     * @return decoded object instantiated from matched registered class
     */

    Object decode(JsonObject encoded) throws Exception //should throw NoMatchedClassException

}
