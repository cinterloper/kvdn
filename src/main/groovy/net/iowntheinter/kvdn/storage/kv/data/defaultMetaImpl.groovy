package net.iowntheinter.kvdn.storage.kv.data

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.kvdata
import net.iowntheinter.kvdn.storage.kv.local.shimAsyncMap
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.storage.meta.metadataStore
import net.iowntheinter.kvdn.storage.txnHook

/**
 * Created by g on 2/9/17.
 * trackables:
 *   mime type
 *   access lists
 *   owner
 *   mtime
 *   version
 *
 * */


class defaultMetaImpl implements metadataStore {
    defaultMetaImpl(kvdata DataImpl){

    }

    @Override
    void setMetadata(String path, Map data, Object cb) {

    }

    @Override
    void setAttr(String path, String name, String data, Object cb) {

    }

    @Override
    void listAttrs(String path, Object cb) {

    }

    @Override
    void removeAttr(String path, String name, Object cb) {

    }

    @Override
    void listMaps(Object cb) {

    }

    @Override
    void write_straddr(Object straddr, Object cb) {

    }

    @Override
    void remove_straddr(Object straddr, Object cb) {

    }
}
