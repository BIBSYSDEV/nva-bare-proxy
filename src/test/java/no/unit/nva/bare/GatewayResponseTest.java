package no.unit.nva.bare;

import org.junit.jupiter.api.Test;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GatewayResponseTest {

    private static final String EMPTY_STRING = "";

    public static final String CORS_HEADER = "CORS header";
    public static final String MOCK_BODY = "mock";
    public static final String ERROR_BODY = "error";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";

//    @Test
    public void testErrorResponse() {
        String expectedJson = ERROR_JSON;
        // calling real constructor (no need to mock as this is not talking to the internet)
        // but helps code coverage
        GatewayResponse gatewayResponse = new GatewayResponse(MOCK_BODY, SC_CREATED);
        gatewayResponse.setErrorBody(ERROR_BODY);
        assertEquals(expectedJson, gatewayResponse.getBody());
    }

    @Test
    public void testNoCorsHeaders() {
        final Config config = Config.getInstance();
        config.setCorsHeader(EMPTY_STRING);
        final String corsHeader = config.getCorsHeader();
        GatewayResponse gatewayResponse = new GatewayResponse(MOCK_BODY, SC_CREATED);
        assertFalse(gatewayResponse.getHeaders().containsKey(GatewayResponse.CORS_ALLOW_ORIGIN_HEADER));
        assertFalse(gatewayResponse.getHeaders().containsValue(corsHeader));

        config.setCorsHeader(CORS_HEADER);
        GatewayResponse gatewayResponse1 = new GatewayResponse(MOCK_BODY, SC_CREATED);
        assertTrue(gatewayResponse1.getHeaders().containsKey(GatewayResponse.CORS_ALLOW_ORIGIN_HEADER));
    }

}
