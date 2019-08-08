package net.iowntheinter.kvdn.storage.meta.impl

import io.vertx.core.Handler
import io.vertx.core.shareddata.LocalMap
import net.iowntheinter.kvdn.KvdnOperation
import net.iowntheinter.kvdn.storage.KvdnSession
import net.iowntheinter.kvdn.storage.meta.MetadataStore
import net.iowntheinter.kvdn.storage.TXNHook


/**
 * Created by g on 2/20/17.
 * here as an idea/example of chaining/seralizing async hooks ... probably not what we actually want to do
 */
class StoreMetaKVHookChain implements TXNHook {
    @Override
    HookType getType() {
        return HookType.META_HOOK
    }

    @Override
    void call(KvdnOperation tx, KvdnSession session, Handler cb) {
        Map elems = tx.metabuffer
        LocalMap state = session.vertx.sharedData().getLocalMap(tx.txid.toString())
        if (!state.get('keys'))
            state.put('keys', elems.keySet().toList())
        if (!state.get('metaElemCnt'))
            state.put('metaElemCnt', tx.metabuffer.size())
        if (!state.get('metaElemPtr'))
            state.put('metaElemPtr', 0)
        String k = (state.get('keys') as List)[state.get('metaElemPtr') as Integer]
        (tx.metaData as MetadataStore).setAttr(tx.strAddr, k, elems[k].toString(), cb)

    }
}
