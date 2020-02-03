package no.unit.nva.bare;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class CreateAuthorityHandlerTest {

    public static final String BODY_KEY = "body";
    public static final String MOCK_BODY = "{\"name\": \"Unit, DotNo\"}";

    @Test
    public void testCreateAuthority() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse gatewayResponse = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(gatewayResponse);
    }
}
