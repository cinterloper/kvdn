package net.iowntheinter.kvdn.mapdb

import net.iowntheinter.kvdn.storage.kv.kvdata
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
interface mapdbData extends kvdata {
    DB getDB()
}
