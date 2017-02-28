package net.iowntheinter.kvdn.mapdb.impl

import net.iowntheinter.kvdn.mapdb.mapdbExtension
import net.iowntheinter.kvdn.storage.kvdnSession
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
class mapdbShutdownHook extends mapdbExtension  {

    def DataImpl
    DB db

    void call(kvdnSession kvdnSession, cb) {
        this.DataImpl = kvdnSession.d
        this.db = (DataImpl as mapdbDataImpl).db
        db.close()
        cb()
    }
}