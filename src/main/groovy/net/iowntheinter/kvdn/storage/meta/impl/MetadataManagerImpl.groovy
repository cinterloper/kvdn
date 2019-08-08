package net.iowntheinter.kvdn.storage.meta.impl

import io.vertx.core.Handler
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.meta.MetadataStore
import net.iowntheinter.kvdn.storage.MetadataManager
import net.iowntheinter.kvdn.storage.TXNHook

/**
 * Created by g on 2/20/17.
 */
class MetadataManagerImpl implements MetadataManager {
    @Override
    void setStore(MetadataStore store) {

    }

    @Override
    Map<String, TXNHook> getHooks() {
        return null
    }

    class startHook implements TXNHook{
        def meta

        @Override
        HookType getType() {
            return HookType.META_HOOK
        }

        @Override
        void call(KvdnOperation tx, KvdnSession session, Handler cb) {

        }
    }
    class endHook implements TXNHook{

        @Override
        HookType getType() {
            return HookType.META_HOOK
        }

        @Override
        void call(KvdnOperation tx, KvdnSession session, Handler cb) {

        }
    }


}
