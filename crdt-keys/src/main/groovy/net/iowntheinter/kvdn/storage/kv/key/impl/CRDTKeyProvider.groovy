package net.iowntheinter.kvdn.storage.kv.key.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.crdts.sets.ORSet
import net.iowntheinter.kvdn.storage.kv.key.keyProvider
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

/**
 * Created by g on 7/12/16.
 */
class CRDTKeyProvider implements keyProvider  {
//this provides a CRDT based implementation of keysets for KVDN maps
//Vert.X does not expose built-in methods for enumerating the keySet() on an async map

    CRDTKeyProvider(Vertx v){
        def eb = v.eventBus();
        def logger = LoggerFactory.getLogger(this.class.getName())
        eb.consumer("_kvdn_keysync", { message -> //listen for updates on this keyset
            logger.trace("got keysync message")
            def b = message.body()
            def updateORSet = b
            def inp = new Input(updateORSet as byte[])
            peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class), strAddr)
        })


    }
    def addListener = { strAddr ->
        eb.consumer("_keysync_${strAddr}", { message -> //listen for updates on this keyset
            logger.trace("got keysync message")
            def b = message.body()
            def updateORSet = b
            def inp = new Input(updateORSet as byte[])
            peerUpdateHdlr(serializer.readObjectOrNull(inp, ORSet.class), strAddr)
        })
    }

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

    void getKeys(cb) {
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
