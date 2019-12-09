package no.unit.nva.bare;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AuthorityProxyTest {

    private String postRequestBody = "{\n" +
            "\"name\": \"Moser, May-Britt\",\n" +
            "\"feideId\": \"may-britt.moser@ntnu.no\",\n" +
            "\"orcId\": \"0000-0001-7884-3049\"\n" +
            "}";

    private String postResponseBody = "{\n" +
            "\"name\": \"Moser, May-Britt\",\n" +
            "\"scn\": \"90517730\",\n" +
            "\"feideId\": \"\",\n" +
            "\"orcId\": \"\",\n" +
            "\"birth date\": \"1963-\"\n" +
            "}";


    @Test
    public void successfulResponse() {
        AuthorityProxy authorityProxy = new AuthorityProxy();
        GatewayResponse result = (GatewayResponse) authorityProxy.handleRequest(postRequestBody, null);
        assertEquals(result.getStatusCode(), 200);
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(postResponseBody, content);
    }
}
