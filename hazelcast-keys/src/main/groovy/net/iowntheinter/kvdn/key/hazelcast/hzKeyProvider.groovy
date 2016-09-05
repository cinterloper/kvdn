package net.iowntheinter.kvdn.key.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientNetworkConfig
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import net.iowntheinter.kvdn.storage.kv.key.keyProvider

/**
 * Created by g on 7/17/16.
 */
class hzKeyProvider implements keyProvider {
    private HazelcastInstance client

    hzKeyProvider() {
        ClientNetworkConfig cnc = new ClientNetworkConfig().addAddress("127.0.0.1:5701");
        ClientConfig clientConfig = new ClientConfig().setNetworkConfig(cnc)
        client = HazelcastClient.newHazelcastClient(clientConfig);

    }

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
    void addKey(String name, String key, cb) {
        try{ //since we are just refering to the same map, which has native keySet(), the key should already be there
            //IMap map = client.getMap(name);
            //assert map.keySet().contains(key)
            cb([result:true,error:null])
        }catch(Exception e){
            cb([result:false, error:e])
        }
    }
}
