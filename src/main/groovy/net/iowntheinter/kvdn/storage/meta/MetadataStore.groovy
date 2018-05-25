package net.iowntheinter.kvdn.storage.meta

/**
 * Created by g on 2/20/17.
 */
interface MetadataStore {

    void setMetadata(String path, Map data, cb)
    void setAttr(String path, String name, String data, cb)
    void listAttrs(String path, cb)
    void removeAttr(String path, String name, cb)
    void listMaps(cb)
    void write_straddr(straddr,cb)
    void remove_straddr(straddr,cb)
}
