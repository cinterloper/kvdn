package net.iowntheinter.kvdn

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kvdnSession

/**
 * Created by g on 9/24/16.
 */
abstract class kvdnTX {
    enum txmode {
        MODE_WRITE,
        MODE_READ,
        MODE_COMPLEX,
        MODE_ADMIN
    }

    boolean dirty
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
    Vertx vertx
    def keyprov
    def session
    def preTxHooks = { kvdnTX tx, cb ->
        ((kvdnSession) session).sessionPreTxHooks(tx,cb)
    }



    void bailTx(context, cb) {
        logger.error("KVTX error: ${getDebug()}")
        (this.session as kvdnSession).finishTx(this, {
            cb([result: null, error: context.error ?: getFlags()])
        })
    }

    protected void startTX(String type, Map params = [:], cb) {
        if (this.dirty)
            throw new Exception("tx has already been invoked, you must create another tx")
        logger.trace("${type}:${strAddr}:${params.toString()}")
        this.dirty = true
        preTxHooks(this, cb)
    }

    Set getFlags() {
        return session.txflags
    }

    boolean checkFlags(txtype) {
        return (!session.txflags.contains(txtype))
    }

    Map getDebug() {
        return [
                txid   : this.txid,
                seid   : ((kvdnSession) this.session).sessionid,
                straddr: this.strAddr,
                flags  : this.flags
        ]
    }
}
