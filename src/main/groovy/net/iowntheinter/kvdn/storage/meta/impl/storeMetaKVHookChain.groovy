package net.iowntheinter.kvdn.storage.meta.impl

import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import net.iowntheinter.kvdn.kvdnTX
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.meta.metadataStore
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 2/20/17.
 * here as an idea/example of chaining/seralizing async hooks ... probably not what we actually want to do
 */
class storeMetaKVHookChain implements txnHook {
    @Override
    void call(kvdnTX tx, kvdnSession session, Object cb) {
        Map elems = tx.metabuffer
        LocalMap state = session.vertx.sharedData().getLocalMap(tx.txid.toString())
        if (!state.get('keys'))
            state.put('keys', elems.keySet().toList())
        if (!state.get('metaElemCnt'))
            state.put('metaElemCnt', tx.metabuffer.size())
        if (!state.get('metaElemPtr'))
            state.put('metaElemPtr', 0)
        String k = (state.get('keys') as List)[state.get('metaElemPtr') as Integer]
        (tx.metaData as metadataStore).setAttr(tx.strAddr, k, elems[k].toString(), cb)

    }
}