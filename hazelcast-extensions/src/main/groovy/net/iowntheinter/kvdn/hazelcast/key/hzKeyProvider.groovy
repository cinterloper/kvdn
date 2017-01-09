package net.iowntheinter.kvdn.hazelcast.key

import com.hazelcast.core.IMap
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.kvdn.hazelcast.hzExtension
import net.iowntheinter.kvdn.storage.kv.key.keyProvider

/**
 * Created by g on 7/17/16.
 */
class hzKeyProvider extends hzExtension implements keyProvider  {

    @Override
    void getKeys(String name, cb) {
        try {
            IMap map = client.getMap(name);
            cb([result:map.keySet(),error:null])
        } catch (e) {
            cb([result:null,error:e])
        }
    }

    @Override
    void deleteKey(String name, String key, cb) {
        try{ //since we are just refering to the same map, which has native keySet(), the key should already be gone
            //IMap map = client.getMap(name);
            //assert !map.keySet().contains(name) //eeeennnnhhhhh this could be very timing-dependent
            cb([result:true,error:null])
        }catch(Exception e){
            cb([result:false, error:e])
        }

    }

    @Override
    void setKey(String name, String key, cb) {
        try{ //since we are just refering to the same map, which has native keySet(), the key should already be there
            //IMap map = client.getMap(name);
            //assert map.keySet().contains(key)
            cb([result:true,error:null])
        }catch(Exception e){
            cb([result:false, error:e])
        }
    }

    @Override
    void load(Vertx vertx, Object o) {

    }

    @Override
    JsonObject register(Vertx vertx) {
        return null
    }
}
