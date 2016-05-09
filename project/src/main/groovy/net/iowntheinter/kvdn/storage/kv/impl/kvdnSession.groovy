package net.iowntheinter.kvdn.storage.kv.impl

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.serializers.JavaSerializer
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.crdts.sets.ORSet
/**
 * Created by grant on 11/30/15.
 */
class kvdnSession {
    Vertx v
    def eb
    def logger
    Kryo serializer
    public Map keysets
    public List maps
    UUID sessionid

    kvdnSession(Vertx vx) {
        v = vx
        sessionid = UUID.randomUUID();
        logger = new LoggerFactory().getLogger("Kvdnsession:${sessionid.toString()}")
        keysets = new HashMap()
        eb = v.eventBus();
        serializer = new Kryo()
        serializer.register(ORSet.class, new JavaSerializer())
        maps = new ArrayList();
    }

    def peerUpdateHdlr(ORSet update, uaddr) {
        def keys = keysets.get(uaddr) as ORSet
        keys.merge(update)
        keysets.put(uaddr, keys)
    }


    KvTx newTx(String strAddr) {
        if (!maps.contains(strAddr)) {
            maps.push(strAddr)
            eb.consumer("_keysync_${strAddr}", { message -> //listen for updates on this keyset
                logger.info("got keysync message")
                def b = message.body()
                def updateORSet = b
                def inp = new Input(updateORSet as byte[])
                peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class), strAddr)
            })
        }

        return (new KvTx(strAddr, this, serializer, v))

    }

}
