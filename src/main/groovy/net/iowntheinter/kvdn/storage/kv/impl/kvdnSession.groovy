package net.iowntheinter.kvdn.storage.kv.impl

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.JavaSerializer
import io.vertx.groovy.core.Vertx
import net.iowntheinter.crdts.CRDT
import net.iowntheinter.crdts.sets.ORSet
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
/**
 * Created by grant on 11/30/15.
 */
class kvdnSession {
    Map keysets
    Vertx v
    Kryo serializer
    kvdnSession(vx){
        keysets = new HashMap();
        v = vx
        serializer = new Kryo()
        serializer.register(ORSet.class,new JavaSerializer())

    }
    KvTx newTx(String strAddr){
        return(new KvTx(strAddr, keysets,serializer, v))
    }

}
