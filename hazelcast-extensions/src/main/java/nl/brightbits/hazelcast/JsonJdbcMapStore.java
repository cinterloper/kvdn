package nl.brightbits.hazelcast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;

/**
 * <p/>
 * Can be used to persist a Hazelcast String-Object map into a SQL database with objects stored in JSON format.
 * The intended use is to store only simple serializable objects (think of Strings, Integers and small shallow classes).
 * <p/>
 *
 * @see SerializingJdbcMapStore for more information
 *
 * @author Ricardo Lindooren
 */
public class JsonJdbcMapStore extends SerializingJdbcMapStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonJdbcMapStore.class);

    private static final String PROPERTY_NAME_JSON_PRETTY = "jsonPretty";

    private Gson gson;

    /**
     * @param mapName    the name of the map that is being persisted.
     *                   The name of the table will have the same name (adding a prefix if specified) but will be limited to 63 chars.
     * @param properties
     * @param initializeConnection if true the database connection will be created during initialization of a new instance of this class
     *                             if false the connection will be created the first time it is actually required
     */
    public JsonJdbcMapStore(String mapName, Properties properties, boolean initializeConnection) {
        super(mapName, properties, initializeConnection);
        initializeGson(properties);
    }

    @Override
    protected String serialize(Serializable obj) {
        return gson.toJson(obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Serializable deSerialize(@SuppressWarnings("rawtypes") Class clazz, String serialized) {
        return (Serializable) gson.fromJson(serialized, clazz);
    }

    private void initializeGson(Properties properties) {
        boolean prettyJson = BooleanUtils.toBoolean(getPropertyValue(PROPERTY_NAME_JSON_PRETTY, properties, true));
        LOGGER.debug("Persisting pretty json value: {}", prettyJson);
        if (prettyJson) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new GsonBuilder().create();
        }
    }
}
