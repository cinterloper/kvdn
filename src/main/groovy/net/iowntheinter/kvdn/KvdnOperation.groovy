package net.iowntheinter.kvdn

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
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
abstract class KvdnOperation {
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
        KV_CLEAR,
        CTR_GET,
        CTR_ADDNGET,
        CTR_GETNADD,
        CTR_COMPSET,
        LCK_ACQUIRE,
        LCK_RELEASE,
        QUEUE_ADD,
        QUEUE_TAKE,
        STACK_PUSH,
        STACK_POP
    }
    enum VALUETYPE {
        STRING,
        JSON,
        NUMBER,
        REFRENCE,    //basically a kvdn straddr if internal, remote url if directref. clients may choose to return an instance of a language mapped object
        JSON_ARRAY,
        CUSTOM_BYTES,       //binary
        TYPEINFO ,     //a metadata value
        INT,          //subclass of number
        FLOAT,        //..
        BOOL,         //..
        NULL
    }
    enum DATATYPE{ // of a map
        STRING_MAP, //default
        TYPECLASS,
        SCHEMA,     //metadata that defines a class of database objects
        OBJECT,     //an instance of a schema
        METADATA    //internal metadata
    }

    boolean dirty = false
    boolean multi = false
    SharedData sd
    Logger logger
    EventBus eb
    String strAddr
    UUID txid
    TXTYPE type
    VALUETYPE valueType
    Vertx vertx
    Map metabuffer = null
    KeyProvider keyprov
    KvdnSession session
    KVData metaData
    List opKeys
    Map<String,Object> options
    String parentTX = null
    boolean transactional = false

    KvdnOperation setTransactional(String parentTX) {
        this.transactional = true
        this.parentTX = parentTX
        return this
    }


    void abortOperation(AsyncResult result, KvdnOperation tx, Handler cb) {
        logger.error("KVTX error: ${getDebug(tx)}")
        logger.error(result.cause() as Exception)
        if (logger.isTraceEnabled())
            (result.cause() as Exception).printStackTrace()
        (this.session as KvdnSession).finishOp(this, {
            cb.handle(result)
        })
    }
    protected void startOperation(VALUETYPE valueType, TXTYPE type, Map params = [:], String key, Object value, Handler cb) {
        assert valueType != null
        String opid = UUID.randomUUID().toString()
        this.opKeys = [key]
        if (this.dirty)
            throw new Exception("kvdnTX has already been invoked, you must create another kvdnTX")
        this.type = type
        logger.trace("${type}:${strAddr}:${params.toString()}")
        this.dirty = true
        ((KvdnSession) session).sessionPreOpHooks(this, cb)
    }

    protected void startOperation(TXTYPE type, Map params = [:], Handler cb) {
        if ("opKeys" in params){
            this.opKeys = params.opKeys as List<String>
        }
        if (this.dirty)
            throw new Exception("kvdnTX has already been invoked, you must create another kvdnTX")
        this.type = type
        logger.trace("${type}:${strAddr}:${params.toString()}")
        this.dirty = true
        ((KvdnSession) session).sessionPreOpHooks(this, cb)
    }

    Set getFlags() {
        return session.txflags
    }

    boolean checkFlags(txtype) {
        return (!session.txflags.contains(txtype))
    }

    def putMeta = { String name, String data ->
        if (!metabuffer)
            metabuffer = [:]
        metabuffer[name] = data
        return this
    } as Closure<KvdnOperation> //fluent

    Map getDebug(KvdnOperation tx) {
        return [
                txid   : tx.txid,
                seid   : ((KvdnSession) this.session).sessionid,
                straddr: tx.strAddr,
                flags  : tx.flags
        ]
    }
}
