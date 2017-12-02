package net.iowntheinter.kvdn

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kvdnSession

/**
 * Created by g on 9/24/16.
 */
abstract class  kvdnTX {
    enum txmode {
        MODE_WRITE,
        MODE_READ,
        MODE_COMPLEX,
        MODE_ADMIN
    }
    enum txtype {
        KV_SUBMIT,
        KV_GET,
        KV_SET,
        KV_KEYS,
        KV_SIZE,
        KV_DEL,
        CTR_GET,
        CTR_ADDNGET,
        CTR_GETNADD,
        CTR_COMPSET,
        LKC_ACQUIRE,
        LCK_RELEASE,
        QUEUE_ADD,
        QUEUE_TAKE,
        STACK_PUSH,
        STACK_POP


    }

    boolean dirty = false
    boolean multi = false
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
    String type
    Vertx vertx
    Map metabuffer = null
    def keyprov
    def session
    def metaData
    LinkedList opKeys

    def preTxHooks = { kvdnTX tx, cb ->
        ((kvdnSession) session).sessionPreTxHooks(tx,cb)
    }



    void bailTx(context, cb) {
        logger.error("KVTX error: ${getDebug()}")
        logger.error(context.error as Exception)
        if(logger.isTraceEnabled())
            (context.error as Exception).printStackTrace()
        (this.session as kvdnSession).finishTx(this, {
            cb([result: null, error: context.error ?: getFlags()])
        })
    }

    protected void startTX(txtype type, Map params = [:], cb) {
        if (this.dirty)
            throw new Exception("tx has already been invoked, you must create another tx")
        this.type = type
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

    def putMeta =  {String name , String data ->
        if (!metabuffer)
            metabuffer = [:]
        metabuffer[name]=data
        return this
    } as Closure<kvdnTX> //fluent

    Map getDebug() {
        return [
                txid   : this.txid,
                seid   : ((kvdnSession) this.session).sessionid,
                straddr: this.strAddr,
                flags  : this.flags
        ]
    }
}
