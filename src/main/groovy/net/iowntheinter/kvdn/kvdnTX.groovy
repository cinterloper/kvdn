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
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    def keyprov
    def Vertx vertx
    def session
    enum txtype {
        MODE_WRITE,
        MODE_READ
    }
    UUID txid
    void bailTx(context, cb) {
        logger.error("KVTX error: ${getDebug()}")
        (this.session as kvdnSession).finishTx(this, {
            cb([result: null, error: context.error ?: getFlags()])
        })
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
