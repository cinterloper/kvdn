package net.iowntheinter.kvdn

import com.sun.istack.internal.NotNull
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.kv.key.KeyProvider

/**
 * Created by g on 9/24/16.
 */
@TypeChecked
@CompileStatic
abstract class KvdnTX {
    enum TXMODE {
        MODE_WRITE,
        MODE_READ,
        MODE_COMPLEX,
        MODE_ADMIN
    }
    enum TXTYPE {
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
    TXTYPE type
    Vertx vertx
    Map metabuffer = null
    @NotNull
    KeyProvider keyprov
    @NotNull
    KvdnSession session
    KVData metaData
    LinkedList opKeys

    Closure preTxHooks = { KvdnTX tx, Handler cb ->
        ((KvdnSession) session).sessionPreTxHooks(tx,cb)
    }



    void bailTx(Map context, Handler cb) {
        logger.error("KVTX error: ${getDebug()}")
        logger.error(context.error as Exception)
        if(logger.isTraceEnabled())
            (context.error as Exception).printStackTrace()
        (this.session as KvdnSession).finishTx(this, {
            cb.handle([result: null, error: context.error ?: getFlags()])
        })
    }

    protected void startTX(TXTYPE type, Map params = [:], cb) {
        if (this.dirty)
            throw new Exception("kvdnTX has already been invoked, you must create another kvdnTX")
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
    } as Closure<KvdnTX> //fluent

    Map getDebug() {
        return [
                txid   : this.txid,
                seid   : ((KvdnSession) this.session).sessionid,
                straddr: this.strAddr,
                flags  : this.flags
        ]
    }
}
