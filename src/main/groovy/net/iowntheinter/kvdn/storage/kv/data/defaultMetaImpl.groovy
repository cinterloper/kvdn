package net.iowntheinter.kvdn.storage.kv.data

import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.storage.meta.metadataStore

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



    @Override
    void setMetadata(String path, Map data,  cb) {
        JsonObject ser = new JsonObject(data)



    }

    @Override
    void setAttr(String path, String name, String data,  cb) {

    }

    @Override
    void listAttrs(String path,  cb) {

    }

    @Override
    void removeAttr(String path, String name,  cb) {

    }
}
