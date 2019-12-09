package no.unit.nva.bare;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthorityProxyTest {

    @Test
    public void successfulResponse() {
        AuthorityProxy authorityProxy = new AuthorityProxy();
        GatewayResponse result = (GatewayResponse) authorityProxy.handleRequest(null, null);
        assertEquals(result.getStatusCode(), 200);
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains("\"message\""));
        assertTrue(content.contains("\"hello world\""));
        assertTrue(content.contains("\"location\""));
    }
}
