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
    enum txtype {
        MODE_WRITE,
        MODE_READ,
        MODE_COMPLEX,
        MODE_ADMIN
    }
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
    boolean dirty
    def keyprov
    def Vertx vertx
    def session


    void bailTx(context, cb) {
        logger.error("KVTX error: ${getDebug()}")
        (this.session as kvdnSession).finishTx(this, {
            cb([result: null, error: context.error ?: getFlags()])
        })
    }
    private void startTX(String type, Map params = [:]){
        if(this.dirty)
            throw new Exception("tx has already been invoked, you must create another tx")
        logger.trace("${type}:${strAddr}:${params.toString()}");
        this.dirty = true
    }

    Set getFlags() {
        return session.txflags
    }

    boolean checkFlags(txtype) {
        return (!session.txflags.contains(txtype))
    }

    Map getDebug(){
        return [
                txid: this.txid,
                seid: ((kvdnSession)this.session).sessionid,
                straddr: this.strAddr,
                flags: this.flags
        ]
    }
}
