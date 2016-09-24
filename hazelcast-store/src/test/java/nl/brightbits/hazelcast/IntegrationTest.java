package nl.brightbits.hazelcast;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

    private final static String SQL_CREATE_DB =
            "CREATE TABLE %s ( \n" +
                    "  key_md5 VARCHAR(32) PRIMARY KEY, \n" +
                    "  key_org VARCHAR(256) NOT NULL, \n" +
                    "  classname VARCHAR(256) NOT NULL, \n" +
                    "  value TEXT NULL)";

    @Test
    public void testDatabaseInteraction() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(JsonJdbcMapStore.PROPERTY_NAME_DB_URL, "jdbc:hsqldb:mem:testdb");
        properties.setProperty(JsonJdbcMapStore.PROPERTY_NAME_DB_USER, "SA");
        final String tablePrefix = "hz_";
        properties.setProperty(JsonJdbcMapStore.PROPERTY_TABLE_NAME_PREFIX, tablePrefix);

        final String mapName = "someMap";

        JsonJdbcMapStoreFactory factory = new JsonJdbcMapStoreFactory();
        JsonJdbcMapStore mapStore = (JsonJdbcMapStore) factory.newMapStore(mapName, properties);

        // Use the connection created by the map store
        Connection connection = ((JsonJdbcMapStore) mapStore).getConnection();
        assertThat(connection, notNullValue());
        assertThat("Connection should be open", !connection.isClosed());

        // Enabling MySQL syntax on HSQLDB connection so that the TEXT type is supported
        connection.createStatement().execute("SET DATABASE SQL SYNTAX MYS TRUE");

        // Create the expected table
        String tableName = tablePrefix + mapName;
        final String createTableStatement = String.format(SQL_CREATE_DB, tableName);
        LOGGER.debug("Going to create table:\n{}", createTableStatement);
        connection.createStatement().execute(createTableStatement);

        assertThat(countFromTable(connection, tableName), is(0));

        mapStore.store("key1", new SimpleObject(new Long(1234), "someObject"));
        assertThat(countFromTable(connection, tableName), is(1));

        mapStore.store("key2", new SimpleObject(new Long(8710), "anotherObject"));
        assertThat(countFromTable(connection, tableName), is(2));

        mapStore.store("key3", new SimpleObject(new Long(19087), "andYetAnotherObject"));
        assertThat(countFromTable(connection, tableName), is(3));

        assertThat(mapStore.loadAllKeys(), hasItems("key1", "key2", "key3"));

        mapStore.deleteAll(Arrays.asList("key1", "key3"));
        assertThat(countFromTable(connection, tableName), is(1));
        assertThat(mapStore.loadAllKeys(), hasItem("key2"));
        assertThat(mapStore.load("key1"), nullValue());
        assertThat(mapStore.load("key3"), nullValue());

        SimpleObject so = (SimpleObject) mapStore.load("key2");
        assertThat(so.getName(), is("anotherObject"));
        assertThat(so.getId(), is(new Long(8710)));

        so.setName("finalObject");
        mapStore.store("key2", so);
        assertThat(countFromTable(connection, tableName), is(1));
        assertThat(mapStore.loadAllKeys(), hasItem("key2"));
        so = (SimpleObject) mapStore.load("key2");
        assertThat(so.getName(), is("finalObject"));
        assertThat(so.getId(), is(new Long(8710)));

        mapStore.delete("key2");
        assertThat(countFromTable(connection, tableName), is(0));
    }

    private int countFromTable(Connection connection, String tableName) {
        try (Statement statement = connection.createStatement()) {
            assertTrue(statement.execute("select count(*) from " + tableName));
            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            LOGGER.error("Count failed", e);
            fail(e.getMessage());
        }
        return -1;
    }
}
