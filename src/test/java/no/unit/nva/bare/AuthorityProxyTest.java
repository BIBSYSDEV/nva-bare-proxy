package no.unit.nva.bare;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityProxyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccessfulResponse() throws IOException {
        String postRequestBody = "{\n" +
                "\"name\": \"May-Britt Moser\",\n" +
                "\"feideId\": \"may-britt.moser@ntnu.no\",\n" +
                "\"orcId\": \"0000-0001-7884-3049\"\n" +
                "}";
        String postResponseBody = "[\n" +
                "  {\n" +
                "    \"name\": \"Moser, May-Britt\",\n" +
                "    \"scn\": \"90517730\",\n" +
                "    \"feideId\": \"\",\n" +
                "    \"orcId\": \"\",\n" +
                "    \"birthDate\": \"1963-\"\n" +
                "  }\n" +
                "]";
        AuthorityProxy mockAuthorityProxy = new AuthorityProxy(mockBareConnection);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(readTestResourceFile("/bareResponse.json"));
        when(mockBareConnection.connect(anyString())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.setUpQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = (GatewayResponse) mockAuthorityProxy.handleRequest(postRequestBody, null);
        assertEquals(result.getStatusCode(), 200);
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testFailingRequest() throws IOException {
        String postRequestBody = "{\n" +
                "\"name\": \"May-Britt Moser\",\n" +
                "\"feideId\": \"\",\n" +
                "\"orcId\": \"\"\n" +
                "}";
        AuthorityProxy mockAuthorityProxy = new AuthorityProxy(mockBareConnection);
        when(mockBareConnection.connect(anyString())).thenThrow(new IOException("my mock throws an exception"));
        when(mockBareConnection.setUpQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = (GatewayResponse) mockAuthorityProxy.handleRequest(postRequestBody, null);
        assertEquals(result.getStatusCode(), 500);
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains("\"my mock throws an exception\""));
    }


    private String readTestResourceFileAsString(String testFileName) throws NullPointerException {
        InputStream inputStream = this.readTestResourceFile(testFileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private InputStream readTestResourceFile(String testFileName) {
        return AuthorityProxyTest.class.getResourceAsStream(testFileName);
    }

}
