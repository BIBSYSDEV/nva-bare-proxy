package no.unit.nva.bare;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class GatewayResponseTest {


    @Test
    public void testErrorResponse() {
        String expectedJson = "{\"error\":\"error\"}";
        // calling real constructor (no need to mock as this is not talking to the internet)
        // but helps code coverage
        GatewayResponse gatewayResponse = new GatewayResponse("mock", Response.Status.CREATED.getStatusCode());
        gatewayResponse.setErrorBody("error");
        assertEquals(expectedJson, gatewayResponse.getBody());
    }

}
