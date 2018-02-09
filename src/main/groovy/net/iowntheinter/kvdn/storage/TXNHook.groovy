package net.iowntheinter.kvdn.storage

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Handler
import net.iowntheinter.kvdn.KvdnTX

/**
 * Created by g on 1/9/17.
 */
@TypeChecked
@CompileStatic
interface TXNHook {
    enum HookType {
        HOOK,
        META_HOOK,
        PLUGIN_HOOK
    }
    HookType getType()
    void call(KvdnTX tx, KvdnSession session, Handler cb)
}
