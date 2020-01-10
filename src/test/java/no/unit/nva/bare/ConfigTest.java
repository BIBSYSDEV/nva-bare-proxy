package no.unit.nva.bare;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigTest {

    @Test(expected = RuntimeException.class)
    public void testCheckPropertiesNothingSet() {
        final Config config = Config.getInstance();
        config.setBareApikey(null);
        config.setBareHost(null);
        config.checkProperties();
        fail();
    }

    @Test
    public void testCorsHeaderNotSet() {
        final Config config = Config.getInstance();
        config.setCorsHeader(null);
        final String corsHeader = config.getCorsHeader();
        assertNull(corsHeader);
    }

    @Test
    public void testCheckPropertiesSet() {
        final Config instance = Config.getInstance();
        instance.setBareHost(Config.BARE_HOST_KEY);
        instance.setBareApikey(Config.BARE_APIKEY_KEY);
        assertTrue(instance.checkProperties());
    }

    @Test(expected = RuntimeException.class)
    public void testCheckPropertiesSetOnlyHostKey() {
        final Config instance = Config.getInstance();
        instance.setBareHost(Config.BARE_HOST_KEY);
        instance.setBareApikey(null);
        instance.checkProperties();
        fail();
    }

    @Test(expected = RuntimeException.class)
    public void testCheckPropertiesSetOnlyApiKey() {
        final Config instance = Config.getInstance();
        instance.setBareHost(null);
        instance.setBareApikey(Config.BARE_APIKEY_KEY);
        instance.checkProperties();
        fail();
    }
}
