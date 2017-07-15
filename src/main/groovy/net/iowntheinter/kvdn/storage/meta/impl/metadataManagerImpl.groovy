package net.iowntheinter.kvdn.storage.meta.impl

import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.meta.metadataStore
import net.iowntheinter.kvdn.storage.metadataManager
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 2/20/17.
 */
class metadataManagerImpl implements metadataManager {
    @Override
    void setStore(metadataStore store) {

    }

    @Override
    Map<String, txnHook> getHooks() {
        return null
    }

    class startHook implements txnHook{
        def meta

        @Override
        void call(kvdnTX tx, kvdnSession session, Object cb) {

        }
    }
    class endHook implements txnHook{

        @Override
        void call(kvdnTX tx, kvdnSession session, Object cb) {

        }
    }


}
