package net.iowntheinter.kvdn.mapdb.impl

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.core.Handler
import net.iowntheinter.kvdn.KvdnTX
import net.iowntheinter.kvdn.mapdb.mapdbExtension

import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.TXNHook
import net.iowntheinter.kvdn.storage.kv.KVData
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
class mapdbShutdownHook extends mapdbExtension implements TXNHook {

    KVData DataImpl
    DB db


    @Override
    void call(KvdnTX KvdnTX, KvdnSession kvs, Handler cb) {
        this.DataImpl = kvs.D
        this.db = (DataImpl as mapdbDataImpl).db
        try{
            db.close()
        }catch (e){
            cb.handle(Future.failedFuture(e))
            return
        }
        cb.handle(Future.succeededFuture())
    }
}