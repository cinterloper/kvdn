package net.iowntheinter.kvdn.storage.kv.key.impl

import io.vertx.core.Vertx
import io.vertx.core.shareddata.SharedData
import net.iowntheinter.kvdn.storage.kv.key.keyProvider

/**
 * Created by g on 7/14/16.
 * this provides a local (single non clustered) keyset manager for kvdn maps
 * its really a noop impl, because it uses keySet() onthe same localMap that kvdn is using for storage
 * this is not a possibility with the native vertx clustered asyncMap impl
 */
class LocalKeyProvider implements keyProvider {
    SharedData sd;
    LocalKeyProvider(Vertx vertx){
        sd = vertx.sharedData()
    }

    @Override
    void getKeys(String name,  cb) {
        cb([result:new ArrayList(sd.getLocalMap(name).keySet()),error:null])
    }

    @Override
    void deleteKey(String map, String name,  cb) {
        try{ //since we are just refering to the same map, which has native keySet(), the key should already be gone
            assert !sd.getLocalMap(map).keySet().contains(name)
            cb([result:true,error:null])
        }catch(Exception e){
            cb([result:false, error:e])
        }

    }

    @Override
    void addKey(String map, String name,  cb) {
        try{ //since we are just refering to the same map, which has native keySet(), the key should already be there
            assert sd.getLocalMap(map).keySet().contains(name)
            cb([result:true,error:null])
        }catch(Exception e){
            cb([result:false, error:e])
        }
    }
}
