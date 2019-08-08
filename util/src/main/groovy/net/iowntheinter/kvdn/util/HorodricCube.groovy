package net.iowntheinter.kvdn.util

import com.esotericsoftware.kryo.*
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.def.KvdnTypeDecoder

class HorodricCube<F, T> implements KvdnTypeDecoder {

    Kryo kryo

    HorodricCube() {
        kryo = new Kryo()
    }

    T encode(F thing) {

    }

    F decode(T thing) {

    }

    Object transmute(Class from, Class to, Object it) {
        kryo.register(from)


    }


    /*
      Input type
      Storage type

     */


    @Override
    void register(Class clazz) {

    }

    @Override
    void register(Class clazz, String matcher) {

    }

    @Override
    Object decode(JsonObject encoded) throws Exception {
        return null
    }
}

