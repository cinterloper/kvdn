package net.iowntheinter.kvdn.storage.kv.key.impl

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.crdts.sets.ORSet
import net.iowntheinter.kvdn.storage.kv.key.keyProvider

/**
 * Created by g on 7/12/16.
 */
class defaultKeyProvider implements keyProvider  {


    def peerUpdateHdlr(ORSet update, uaddr) {
        ORSet keys = KeySets.get(uaddr) as ORSet
        if(keys == null)
            keys = new ORSet()
        keys.merge(update)
        KeySets.put(uaddr, keys)
    }
    def peerUpdateHdlr(ORSet update, uaddr,cb) {
        ORSet keys = KeySets.get(uaddr) as ORSet
        if(keys == null)
            keys = new ORSet()
        keys.merge(update)
        KeySets.put(uaddr, keys)
        cb(new JsonObject().put("result", new JsonArray(keys.get().toList())))
    }
    def keyReqHandler = { message ->
        logger.trace("Got a remote key req: ${message.body().strAddr}")
        Map keyReqCtx = message.body()
        def strAddr = keyReqCtx["strAddr"]
        def baos = new ByteArrayOutputStream();
        def out = new Output(baos);
        def keys = KeySets.get(strAddr)
        serializer.writeObjectOrNull(out, keys, ORSet.class)
        def replContents = baos.toByteArray()
        message.reply(replContents)
        logger.trace("replyed to keyreq ")
    }

    Object getKeys(cb) {
        def atmt = KeySets.get(strAddr)
        if (atmt != null)
            keys = atmt as ORSet
        if (keys == null) {

            eb.send("keyreq_${strAddr}", new JsonObject([strAddr: strAddr]), { resp ->
                if(resp.result()) {
                    def data = resp.result().body()
                    logger.trace("got response from keyreq_${strAddr}: err if any: ${resp.cause()}  ")
                    def inp = new Input(data as byte[])
                    peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class), strAddr, cb)
                }
                else{
                    logger.error("no response from key request")
                    cb(new JsonObject().put("error","no response from key request"))
                }
            })
        } else
            cb(new JsonObject().put("result", new JsonArray(keys.get().toList())))

    }

    @Override
    void getKeys(String name, Object cb) {

    }

    @Override
    void deleteKey(String map, String name, Object cb) {

    }

    @Override
    void addKey(String map, String name, Object cb) {

    }
}
