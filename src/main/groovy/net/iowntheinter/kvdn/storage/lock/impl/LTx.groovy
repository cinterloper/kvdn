package net.iowntheinter.kvdn.storage.lock.impl

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.Counter
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.lock.TXLCK

class LTx extends kvdnTX implements TXLCK {
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
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

        void getLock(LTx txn, cb) {
            vertx = txn.vertx
            name = txn.strAddr
            this.sd = vertx.sharedData()
            sd.getLock("${name}", cb)
            logger.trace("starting  kvdn lock operation ${name}, ${txid} with vertx.isClustered() == ${vertx.isClustered()}")

        }
    }

    def LTx(String sa, UUID txid, kvdnSession session, Vertx vertx) {
        // keys = new ORSet()
        this.vertx = vertx
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


    @Override
    void release(Lock l,  cb) {
        try{
            assert checkFlags(txtype.MODE_WRITE)
            l.release()
            cb([result:true,error:null])
        }catch(e){
            cb([result:false,error:e])
        }
    }

    Set getFlags() {
        return session.txflags
    }

    boolean checkFlags(txtype) {
        return (!session.txflags.contains(txtype))
    }


    @Override
    void get(cb) {
        D.getLock(this, { res ->
            if (res.succeeded() && checkFlags(txtype.MODE_READ)) {
                Counter ctr = res.result();
                ctr.get({ resGet ->
                    if (resGet.succeeded()) {
                        logger.trace("get:${strAddr}")
                        (this.session as kvdnSession).finishTx(this)
                        cb([result: resGet.result().toString(), error: null])
                    } else {
                        bailTx([txid: this.txid, straddr: strAddr, session: this.session, flags: session.txflags])
                        cb([result: null, error: resGet.cause()])
                    }
                })
            } else {
                bailTx([txid: this.txid, straddr: strAddr, session: this.session, flags: session.txflags])
                cb([result: null, error: res.cause()])
            }
        })
    }


}