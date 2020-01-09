package no.unit.nva.bare;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GatewayResponseTest {


    public static final String CORS_HEADER = "CORS header";
    public static final String MOCK_BODY = "mock";
    public static final String ERROR_BODY = "error";
    public static final String ERROR_JSON = "{\"error\":\"error\"}";

    @Test
    public void testErrorResponse() {
        String expectedJson = ERROR_JSON;
        // calling real constructor (no need to mock as this is not talking to the internet)
        // but helps code coverage
        GatewayResponse gatewayResponse = new GatewayResponse(MOCK_BODY, Response.Status.CREATED.getStatusCode());
        gatewayResponse.setErrorBody(ERROR_BODY);
        assertEquals(expectedJson, gatewayResponse.getBody());
    }

    @Test
    public void testNoCorsHeaders() {
        GatewayResponse gatewayResponse = new GatewayResponse(MOCK_BODY, Response.Status.CREATED.getStatusCode());
        assertFalse(gatewayResponse.getHeaders().containsKey(GatewayResponse.CORS_ALLOW_ORIGIN_HEADER));
    }

    @Test
    public void testCorsHeaders() {
        GatewayResponse gatewayResponse = new GatewayResponse(CORS_HEADER);
        assertTrue(gatewayResponse.getHeaders().containsKey(GatewayResponse.CORS_ALLOW_ORIGIN_HEADER));
    }

}
