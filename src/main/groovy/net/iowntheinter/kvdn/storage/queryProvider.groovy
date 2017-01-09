package net.iowntheinter.kvdn.storage
import net.iowntheinter.cornerstone.util.extensionManager.extension

/**
 * Created by g on 1/8/17.
 */
interface queryProvider extends extension {
    void query(String addr, String query, cb)
}
