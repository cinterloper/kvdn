package nl.brightbits.hazelcast;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * <p/>
 * Can be used to persist a Hazelcast String-Object map into a SQL database with objects stored in serialized format.
 * <p/>
 * <p/>
 * The table name is based on the map name. It can be prefixed (e.g. with 'hz_') by providing a value for
 * <ul>
 * <li>'tableNamePrefix' in the properties of the map (configured in the maps XML configuration) </li>
 * <li>'hz.mapstore.jsonjdbc.tableNamePrefix' java environment property (configured as a jvm startup argument for example: -Dhz.mapstore.jsonjdbc.tableNamePrefix=hz_</li>
 * <li>'HZ_MAPSTORE_JSONJDBC_TABLENAMEPREFIX' system environment variable</li>
 * </ul>
 * Environment variables will be used in favor of system properties, which in turn will be used in favor of values the map properties.
 * <p/>
 * <i>This class expects the table to already exist (you have to create it yourself)</i>
 * For example in MySQL you can create a table like this:
 * <pre>
 *     CREATE TABLE name_of_your_map (
 *         key_md5 VARCHAR(32) PRIMARY KEY,
 *         key_org VARCHAR(256) NOT NULL,
 *         classname VARCHAR(256) NOT NULL,
 *         value TEXT NULL,
 *         lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
 *     )
 * </pre>
 * where the 'lastUpdated' column is optional.
 * <p/>
 * <p/>
 * <p>
 * <b>Please note that this implementation isn't optimized for large maps.</b>
 * </p>
 *
 * @author Ricardo Lindooren
 */
public abstract class SerializingJdbcMapStore implements MapStore<String, Serializable>, MapLoader<String, Serializable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializingJdbcMapStore.class);

    public static final String PROPERTY_SYSTEM_ENV_PREFIX = "hz.mapstore.jdbc.";
    public static final String PROPERTY_NAME_DB_DRIVER = "dbDriver";
    public static final String PROPERTY_NAME_DB_URL = "dbUrl";
    public static final String PROPERTY_NAME_DB_USER = "dbUser";
    public static final String PROPERTY_NAME_DB_PASS = "dbPass";
    public static final String PROPERTY_TABLE_NAME_PREFIX = "tableNamePrefix";

    private static final String SQL_PS_CHECK_KEY_EXISTS = "select 1 from %s where key_md5=?";
    private static final String SQL_PS_LOAD_OBJECT = "select classname, value from %s where key_md5=?";
    private static final String SQL_PS_LOAD_ALL_KEYS = "select key_org from %s";
    private static final String SQL_PS_INSERT_VALUE_FOR_KEY = "insert into %s (key_md5, key_org, classname, value) values(?, ?, ?, ?)";
    private static final String SQL_PS_UPDATE_VALUE_FOR_KEY = "update %s set classname=?, value=? where key_md5=?";
    private static final String SQL_PS_DELETE_VALUE_FOR_KEY = "delete from %s where key_md5=?";

    private String mapName;

    private String sqlCheckIfKeyExists;
    private String sqlLoadObject;
    private String sqlLoadAllKeys;
    private String sqlUpdateValueForKey;
    private String sqlInsertValueForKey;
    private String sqlDeleteValueForKey;

    private String dbUrl;
    private String dbUser;
    private String dbPass;

    private Connection _connection;

    /**
     * @param mapName          the name of the map that is being persisted.
     *                         The name of the table will have the same name (adding a prefix if specified) but will be limited to 63 chars.
     * @param properties       properties of the map, configured in XML under the map-store tag
     * @param openDbConnection if true the database connection will be opened during initialization of a new instance of this class
     */
    public SerializingJdbcMapStore(final String mapName, Properties properties, boolean openDbConnection) {
        if (StringUtils.isBlank(mapName)) {
            throw new IllegalArgumentException("Argument mapName is required");
        }
        this.mapName = mapName;
        LOGGER.debug("Initializing map store for map '{}'...", mapName);
        initializeDatabaseParameters(properties);
        initializeSqlStatements(properties);

        String dbDriver = getPropertyValue(PROPERTY_NAME_DB_DRIVER, properties, true);
        if (StringUtils.isNotBlank(dbDriver)) {
            try {
                LOGGER.debug("Going to load DB driver '" + dbDriver + "'");
                Class.forName(dbDriver);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not load DB driver '" + dbDriver + "'", e);
            }
        }

        if (openDbConnection) {
            try {
                getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Error while obtaining connection to database", e);
            }
        }

        LOGGER.debug("Initialized map store for map '{}'", mapName);
    }

    @Override
    public Serializable load(String key) {
        Serializable object = null;
        if (key != null) {
            try (PreparedStatement ps = getConnection().prepareStatement(sqlLoadObject)) {
                ps.setString(1, createMd5(key));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String className = rs.getString(1);
                    String serializedValue = rs.getString(2);
                    object = deSerialize(Class.forName(className), serializedValue);
                    LOGGER.debug("Loaded object '{}' for key '{}'", object, key);
                }
            } catch (Exception ex) {
                LOGGER.error("Error while loading for key '" + key + "'", ex);
            }
        } else {
            LOGGER.warn("Key was null, returning null");
        }
        return object;
    }

    @Override
    public Map<String, Serializable> loadAll(Collection<String> keys) {
        Map<String, Serializable> result = null;
        if (keys != null) {
            try {
                result = new HashMap<>(keys.size());
                for (String key : keys) {
                    Serializable obj = load(key);
                    result.put(key, obj);
                }
            } catch (Exception ex) {
                LOGGER.error("Error while loading all for keys: " + keys, ex);
            }
        }
        return result;
    }

    @Override
    public Set<String> loadAllKeys() {
        Set<String> allKeys = new HashSet<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sqlLoadAllKeys)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString(1);
                LOGGER.debug("Loaded key '{}'", key);
                allKeys.add(key);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while loading all keys", ex);
        }
        return allKeys;
    }

    @Override
    public void store(String key, Serializable value) {
        try {
            store(getConnection(), key, value);
        } catch (Exception ex) {
            LOGGER.error("Error while storing value '" + value + "' for key '" + key + "'", ex);
        }
    }

    @Override
    public void storeAll(Map<String, Serializable> map) {
        if (map != null) {
            String key = null;
            Serializable value = null;
            try {
                for (String keyFromMap : map.keySet()) {
                    key = keyFromMap;
                    value = map.get(key);
                    store(getConnection(), key, value);
                }
            } catch (Exception ex) {
                LOGGER.error("Error while storing value '" + value + "' for key '" + key + "'", ex);
            }
        }
    }

    private void store(Connection connection, String key, Serializable value) {
        try (PreparedStatement ps = createStorePreparedStatement(connection, key, value)) {
            if (ps.executeUpdate() > 0) {
                LOGGER.debug("Stored for key '{}': {}", key, (value != null ? serialize(value) : null));
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } else {
                LOGGER.warn("Nothing stored for key '{}': {}", key, (value != null ? serialize(value) : null));
            }
        } catch (Exception ex) {
            LOGGER.error("Error while storing value '" + value + "' for key '" + key + "'", ex);
        }
    }

    private PreparedStatement createStorePreparedStatement(Connection connection, String key, Serializable value) throws SQLException {
        final String key_md5 = createMd5(key);
        PreparedStatement ps;
        if (keyExists(key)) {
            ps = connection.prepareStatement(sqlUpdateValueForKey);
            if (value == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, getClassName(value));
                ps.setString(2, serialize(value));
            }
            ps.setString(3, key_md5);
        } else {
            ps = connection.prepareStatement(sqlInsertValueForKey);
            ps.setString(1, key_md5);
            ps.setString(2, key);
            if (value == null) {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(3, getClassName(value));
                ps.setString(4, serialize(value));
            }
        }
        return ps;
    }

    @Override
    public void delete(String key) {
        try (PreparedStatement ps = getConnection().prepareStatement(sqlDeleteValueForKey)) {
            ps.setString(1, createMd5(key));
            if (ps.executeUpdate() > 0) {
                LOGGER.debug("Deleted for key '{}'", key);
            } else {
                LOGGER.warn("Nothing deleted for key '{}'", key);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while deleting entry for key '" + key + "'", ex);
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        if (keys != null) {
            String key = null;
            try {
                for (String keyFromMap : keys) {
                    key = keyFromMap;
                    delete(key);
                }
            } catch (Exception ex) {
                LOGGER.error("Error while deleting entry for key '" + key + "'", ex);
            }
        }
    }

    private boolean keyExists(String key) {
        try (PreparedStatement ps = getConnection().prepareStatement(sqlCheckIfKeyExists)) {
            ps.setString(1, createMd5(key));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception ex) {
            LOGGER.error("Error while checking if key '" + key + "' exists", ex);
        }
        return false;
    }

    private String normalizeTableName(final String name) {
        return StringUtils.left(name, 63);
    }

    protected abstract String serialize(Serializable obj);

    protected abstract Serializable deSerialize(@SuppressWarnings("rawtypes") Class clazz, String serialized);

    private String getClassName(Object obj) {
        return obj.getClass().getName();
    }

    private String createMd5(String data) {
        return DigestUtils.md5Hex(data);
    }

    protected synchronized Connection getConnection() throws SQLException {
        boolean createConnection = false;

        if (_connection == null) {
            createConnection = true;
            LOGGER.info("Database connection doesn't exist yet, going to create it");
        } else if (_connection.isClosed()) {
            createConnection = true;
            LOGGER.warn("Database connection exists already but it is clodes, going to create it");
        }

        if (createConnection) {
            _connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            LOGGER.debug("Created connection: {}", _connection);
        }
        return _connection;
    }

    protected void initializeSqlStatements(Properties properties) {
        final String tableNamePrefix = getPropertyValue(PROPERTY_TABLE_NAME_PREFIX, properties, true);
        final String tableName = normalizeTableName(StringUtils.join(tableNamePrefix, mapName));
        LOGGER.info("Using table name '{}'", tableName);

        sqlCheckIfKeyExists = String.format(SQL_PS_CHECK_KEY_EXISTS, tableName);
        sqlLoadObject = String.format(SQL_PS_LOAD_OBJECT, tableName);
        sqlLoadAllKeys = String.format(SQL_PS_LOAD_ALL_KEYS, tableName);
        sqlInsertValueForKey = String.format(SQL_PS_INSERT_VALUE_FOR_KEY, tableName);
        sqlUpdateValueForKey = String.format(SQL_PS_UPDATE_VALUE_FOR_KEY, tableName);
        sqlDeleteValueForKey = String.format(SQL_PS_DELETE_VALUE_FOR_KEY, tableName);
    }

    private void initializeDatabaseParameters(Properties properties) {
        dbUrl = getPropertyValue(PROPERTY_NAME_DB_URL, properties, true);
        if (StringUtils.isBlank(dbUrl)) {
            throw new IllegalArgumentException("A database URL is required. " +
                    "Provide as value for key '" + PROPERTY_NAME_DB_URL + "' in map store XML configuration, " +
                    "or as value for system environment property '" + PROPERTY_SYSTEM_ENV_PREFIX + PROPERTY_NAME_DB_URL + "'");
        }

        dbUser = getPropertyValue(PROPERTY_NAME_DB_USER, properties, true);
        dbPass = getPropertyValue(PROPERTY_NAME_DB_PASS, properties, false);
    }

    protected String getPropertyValue(String propertyName, Properties mapProperties, boolean logValue) {
        return getPropertyValue(propertyName, System.getenv(), System.getProperties(), mapProperties, logValue);
    }

    protected static String getPropertyValue(String propertyName, Map<String, String> envVariables, Properties systemProperties, Properties mapProperties, boolean logValue) {
        final String valueHiddenForLog = "********";
        final String systemPropertyName = PROPERTY_SYSTEM_ENV_PREFIX + propertyName;
        final String envPropertyName = StringUtils.upperCase(StringUtils.replace(systemPropertyName, ".", "_"));

        if (envVariables != null) {
            String value = envVariables.get(envPropertyName);
            if (StringUtils.isNotBlank(value)) {
                LOGGER.debug("Found '{}' = '{}' in environment variables", envPropertyName, logValue ? value : valueHiddenForLog);
                return value;
            }
            LOGGER.debug("No value found for '{}' in environment variables", envPropertyName);
        }

        if (systemProperties != null) {
            String value = (String) systemProperties.get(systemPropertyName);
            if (StringUtils.isNotBlank(value)) {
                LOGGER.debug("Found '{}' = '{}' in system properties", systemPropertyName, logValue ? value : valueHiddenForLog);
                return value;
            }
            LOGGER.debug("No value found for '{}' in system properties", systemPropertyName);
        }

        if (mapProperties != null) {
            String value = (String) mapProperties.get(propertyName);
            if (StringUtils.isNotBlank(value)) {
                LOGGER.debug("Found '{}' = '{}' in map properties", propertyName, logValue ? value : valueHiddenForLog);
                return value;
            }
            LOGGER.debug("No value found for '{}' in map properties", propertyName);
        }

        return null;
    }
}
