package net.iowntheinter.kvdn.storage.kv.impl

import io.vertx.core.Vertx
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.SharedData
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kv.TXKV
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap
import net.iowntheinter.kvdn.storage.kvdnSession

import java.security.MessageDigest

/**
 * Created by grant on 11/19/15.
 */

class KvTx extends kvdnTX implements TXKV {
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
    def  keyprov
    def Vertx vertx
    def D = new _data_impl()
    def session
    enum txtype {
        MODE_WRITE,
        MODE_READ
    }
    private class _data_impl {
        SharedData sd;
        Vertx vertx
        String name

        void getMap(KvTx txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            if (vertx.isClustered()) {  //vertx cluster mode
                this.sd = vertx.sharedData()
                sd.getClusterWideMap("${name}", cb)
                logger.trace("starting clustered kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
            } else {                    // vertx local mode
                logger.trace("starting local kvdn operation with vertx.isClustered() == ${vertx.isClustered()}")
                cb(Future.succeededFuture(new shimAsyncMap(vertx, name)))
            }
        }
    }

    def KvTx(String sa, UUID txid, kvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
        this.keyprov = session.keyprov
        this.session = session as kvdnSession
        this.txid = txid
        strAddr = sa
        logger = new LoggerFactory().getLogger("KvTx:" + strAddr)
        sd = vertx.sharedData() as SharedData
        eb = vertx.eventBus() as EventBus
    }

    @Override
    Object bailTx(Object context) {
        logger.error("KVTX error: ${this.session} " + context as Map)
        (this.session as kvdnSession).finishTx(this)
        return null
    }
    Set getFlags(){
        return session.txflags
    }
    boolean checkFlags(txtype){
        return (!session.txflags.contains(txtype))
    }
    @Override
    void snapshot() {}

    @Override
    void submit(content, cb) {
        D.getMap(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                def String key = MessageDigest.getInstance("MD5").digest(Buffer.buffer(content.toString()).getBytes()).encodeHex().toString()

                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {

                        keyprov.addKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key',key))
                            (this.session as kvdnSession).finishTx(this)
                            cb([result: resPut.result().toString(), key: key, error: null])
                        })

                    } else {
                        bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                cb([result: null, error: (res.cause() ?: getFlags())])
            }
        })
    }

    @Override
    void set(String key, content, cb) {
        D.getMap(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                map.put(key, content, { resPut ->
                    if (resPut.succeeded()) {
                        keyprov.addKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_+${strAddr}", new JsonObject().put('key',key))
                            (this.session as kvdnSession).finishTx(this)
                            cb([result: resPut.result().toString(), key: key, error: null])
                        })
                    } else {
                        bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                        cb([result: null, error: resPut.cause()])
                    }
                })
            } else {
                bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                cb([result: null, error: res.cause()])
            }
        })
    }



    @Override
    void get(String key, cb) {
        D.getMap(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                def AsyncMap map = res.result();
                map.get(key, { resGet ->
                    if (resGet.succeeded()) {
                        logger.trace("get:${strAddr}:${key}")
                        (this.session as kvdnSession).finishTx(this)
                        cb([result: resGet.result().toString(), error: null])
                    } else {
                        bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                cb([result: null, error: res.cause()])
            }
        })
    }
    @Override
    void del(String key, cb) {
        D.getMap(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_WRITE)) {
                def AsyncMap map = res.result();
                map.remove(key, { resDel ->
                    if (resDel.succeeded()) {
                        keyprov.deleteKey(strAddr, key, {
                            logger.trace("set:${strAddr}:${key}");
                            eb.publish("_KVDN_-${strAddr}", new JsonObject().put('key'))
                            (this.session as kvdnSession).finishTx(this)
                            cb([result: resDel.result().toString(), key: key, error: null])
                        })
                    } else {
                        bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                        cb([result: null, error: resDel.cause()])
                    }
                })
            } else {
                bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                cb([result: null, error: res.cause()])
            }
        })
    }
    @Override
    void getKeys(cb) {
        keyprov.getKeys(strAddr, { Map asyncResult ->
            (this.session as kvdnSession).finishTx(this)
            cb(asyncResult)
        })
    }
    @Override
    void size(cb) {
        D.getMap(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                def AsyncMap map = res.result();
                map.size( { resGet ->
                    if (resGet.succeeded()) {
                        logger.trace("size:${strAddr}")
                        (this.session as kvdnSession).finishTx(this)
                        cb([result: resGet.result().toString(), error: null])
                    } else {
                        bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                bailTx([txid:this.txid,straddr:strAddr,session:this.session,flags:session.txflags])
                cb([result: null, error: res.cause()])
            }
        })
    }

}