package net.iowntheinter.kvdn.storage.kv.data

import net.iowntheinter.kvdn.storage.kv.KVData
import net.iowntheinter.kvdn.storage.meta.MetadataStore

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


class DefaultMetaImpl implements MetadataStore {
    DefaultMetaImpl(KVData DataImpl){

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
