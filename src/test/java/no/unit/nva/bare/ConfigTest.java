package no.unit.nva.bare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigTest {

    @Test
    public void testCheckPropertiesNothingSet() {
        final Config config = Config.getInstance();
        config.setBareApikey(null);
        config.setBareHost(null);
        assertThrows(RuntimeException.class, config::checkProperties);
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

    @Test
    public void testCheckPropertiesSetOnlyHostKey() {
        final Config instance = Config.getInstance();
        instance.setBareHost(Config.BARE_HOST_KEY);
        instance.setBareApikey(null);
        assertThrows(RuntimeException.class, instance::checkProperties);
    }

    @Test
    public void testCheckPropertiesSetOnlyApiKey() {
        final Config instance = Config.getInstance();
        instance.setBareHost(null);
        instance.setBareApikey(Config.BARE_APIKEY_KEY);
        assertThrows(RuntimeException.class, instance::checkProperties);
    }
}
