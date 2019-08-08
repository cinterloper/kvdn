package nl.brightbits.hazelcast;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class JsonJdbMapStoreTest {

    @Test
    public void testDatabaseUrlRequiredOnInit() {
        try {
            new JsonJdbcMapStore("someMap", null, false);
            fail("A database url should be required");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), startsWith("A database URL is required"));
        }
    }

    @Test
    public void testSerializeAndDeserialize() {
        Properties mockedProperties = new Properties();
        mockedProperties.put(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, "someUrl");

        JsonJdbcMapStore mapStore = new JsonJdbcMapStore("someMap", mockedProperties, false);

        String json = mapStore.serialize(new SimpleObject(123l, "JustASimpleString"));
        assertThat(json, is("{\"id\":123,\"name\":\"JustASimpleString\"}"));

        SimpleObject so = (SimpleObject) mapStore.deSerialize(SimpleObject.class, json);
        assertThat(so.getId(), is(new Long(123)));
        assertThat(so.getName(), is("JustASimpleString"));
    }

    @Test
    public void testInitializeSqlStatements() throws IllegalAccessException {
        Properties mockedProperties = new Properties();
        mockedProperties.put(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, "someUrl");

        JsonJdbcMapStore mapStore = new JsonJdbcMapStore("someMap", mockedProperties, false);

        mapStore.initializeSqlStatements(null);
        String sql = (String) FieldUtils.readField(mapStore, "sqlLoadAllKeys", true);
        assertThat(sql, is("select key_org from someMap"));
    }
}
