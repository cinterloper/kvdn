package net.iowntheinter.kvdn.mapdb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import jetbrains.exodus.env.Store
import net.iowntheinter.kvdn.storage.kv.KVData

/**
 * Created by g on 1/29/17.
 */
@TypeChecked
@CompileStatic
interface XodusData extends KVData {
    Store getDB()
}
