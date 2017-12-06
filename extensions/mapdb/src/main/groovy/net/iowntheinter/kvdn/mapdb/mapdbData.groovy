package net.iowntheinter.kvdn.mapdb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import net.iowntheinter.kvdn.storage.kv.KVData
import org.mapdb.DB

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
interface mapdbData extends KVData {
    DB getDB()
}
