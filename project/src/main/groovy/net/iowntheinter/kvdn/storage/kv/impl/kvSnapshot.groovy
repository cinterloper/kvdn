package net.iowntheinter.kvdn.storage.kv.impl

/**
 * Created by grant on 11/15/15.
 */
//when createing snapshot:
// need cow-style key writeing
// we have to traverse a snaped view of the keys, and lock a version of each data to a key name
// -- if no dat available for that keyname, just skip over it and remove it from the key set
// -- store this erratum somewhere

class kvSnapshot implements Map{
    public final long txId

   kvSnapshot(String mapId, int snapTxCtrId){
       txId=snapTxCtrId;
   }
    @Override
    int size() {
        return 0
    }

    @Override
    boolean isEmpty() {
        return false
    }

    @Override
    boolean containsKey(Object o) {
        return false
    }

    @Override
    boolean containsValue(Object o) {
        return false
    }

    @Override
    Object get(Object o) {
        return null
    }

    @Override
    Object put(Object o, Object o2) {
        return null
    }

    @Override
    Object remove(Object o) {
        return null
    }

    @Override
    void putAll(Map map) {

    }

    @Override
    void clear() {

    }

    @Override
    Set keySet() {
        return null
    }

    @Override
    Collection values() {
        return null
    }

    @Override
    Set<Map.Entry> entrySet() {
        return null
    }
}
