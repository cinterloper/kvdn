package net.iowntheinter.kvdn.storage;

import io.vertx.core.Handler;
import net.iowntheinter.kvdn.KvdnOperation;

/**
 * Created by g on 1/9/17.
 */
public interface TXNHook{
    HookType getType();

    void call(KvdnOperation tx, KvdnSession session, Handler cb);

    enum HookType {
        HOOK, META_HOOK, PLUGIN_HOOK
    }
}
