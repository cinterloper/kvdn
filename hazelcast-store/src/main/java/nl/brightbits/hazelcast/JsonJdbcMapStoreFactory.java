package nl.brightbits.hazelcast;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

import java.util.Properties;

public class JsonJdbcMapStoreFactory implements MapStoreFactory {
    @Override
    public MapLoader newMapStore(String mapName, Properties properties) {
        return new JsonJdbcMapStore(mapName, properties, true);
    }
}
