package nl.brightbits.hazelcast;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SerializingJdbcMapStoreTest {

    public static final String URL_A = "urlA";
    public static final String URL_B = "urlB";
    public static final String URL_C = "urlC";

    @Test
    public void testGetPropertyValueFromEnvironmentVariables() {
        final String dbUrlEnvVariableName = StringUtils.upperCase( StringUtils.replace( SerializingJdbcMapStore.PROPERTY_SYSTEM_ENV_PREFIX+ SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, ".", "_" ) );
        Map<String, String> mockedEnvProperties = new HashMap<>();
        mockedEnvProperties.put(dbUrlEnvVariableName, URL_A);
        String value = SerializingJdbcMapStore.getPropertyValue(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, mockedEnvProperties, null, null, true);
        assertThat(value, is(URL_A));
    }

    @Test
    public void testGetPropertyValueFromSystemProperties() {
        final String dbUrlSystemPropName = SerializingJdbcMapStore.PROPERTY_SYSTEM_ENV_PREFIX+ SerializingJdbcMapStore.PROPERTY_NAME_DB_URL;
        Properties mockedSystemProperties = new Properties();
        mockedSystemProperties.put(dbUrlSystemPropName, URL_B);
        String value = SerializingJdbcMapStore.getPropertyValue(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, null, mockedSystemProperties, null, true);
        assertThat(value, is(URL_B));
    }

    @Test
    public void testGetPropertyValueFromMapProperties() {
        Properties mockedMapProperties = new Properties();
        mockedMapProperties.put(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, URL_C);
        String value = SerializingJdbcMapStore.getPropertyValue(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, null, null, mockedMapProperties, true);
        assertThat(value, is(URL_C));
    }

    @Test
    public void testGetPropertyValuePreference() {
        final String dbUrlSystemPropName = SerializingJdbcMapStore.PROPERTY_SYSTEM_ENV_PREFIX+ SerializingJdbcMapStore.PROPERTY_NAME_DB_URL;
        Properties mockedSystemProperties = new Properties();
        mockedSystemProperties.put(dbUrlSystemPropName, URL_B);

        Properties mockedMapProperties = new Properties();
        mockedMapProperties.put(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, URL_C);

        String value = SerializingJdbcMapStore.getPropertyValue(SerializingJdbcMapStore.PROPERTY_NAME_DB_URL, null, mockedSystemProperties, mockedMapProperties, true);
        assertThat("Property from environment should have been chosen", value, is(URL_B));
    }
}
