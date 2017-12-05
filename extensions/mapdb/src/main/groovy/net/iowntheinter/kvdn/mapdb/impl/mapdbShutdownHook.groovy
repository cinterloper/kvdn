package net.iowntheinter.kvdn.mapdb.impl

import io.vertx.core.Handler
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.mapdb.mapdbExtension

import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.txnHook
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
class mapdbShutdownHook extends mapdbExtension implements txnHook {

    def DataImpl
    DB db


    @Override
    void call(kvdnTX kvdnTX, kvdnSession kvs, Handler cb) {
        this.DataImpl = kvs.d
        this.db = (DataImpl as mapdbDataImpl).db
        try{
            db.close()
        }catch (e){

        }
        cb()
    }
}