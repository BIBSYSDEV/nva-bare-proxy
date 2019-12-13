package no.unit.nva.bare;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        AuthorityProxy mockAuthorityProxy = new AuthorityProxy(mockBareConnection);
        InputStream inputStream = AuthorityProxyTest.class.getResourceAsStream("/bareResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.setUpQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"may-britt.moser@ntnu.no\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = (GatewayResponse) mockAuthorityProxy.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get("Content-Type"), "application/json");
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = "[\n"
                + "  {\n"
                + "    \"name\": \"Moser, May-Britt\",\n"
                + "    \"scn\": \"90517730\",\n"
                + "    \"feideId\": \"\",\n"
                + "    \"orcId\": \"\",\n"
                + "    \"birthDate\": \"1963-\"\n"
                + "  }\n"
                + "]";
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testFailingRequest() throws IOException {
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"\"\n"
                + "}";
        AuthorityProxy mockAuthorityProxy = new AuthorityProxy(mockBareConnection);
        when(mockBareConnection.connect(any())).thenThrow(new IOException("my mock throws an exception"));
        when(mockBareConnection.setUpQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = (GatewayResponse) mockAuthorityProxy.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, result.getStatus());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains("\"my mock throws an exception\""));
    }

    @Test
    public void testGetValueFromJson() {
        AuthorityProxy authorityProxy = new AuthorityProxy();
        String testKey1 = "testKey1";
        String testValue1 = "testValue1";
        String testKey2 = "testKey2";
        String testValue2 = "testValue2";
        String testArray = "{\"" + testKey1 + "\": [\"" + testValue1 + "\"],"
                + "\"" + testKey2 + "\": [\"" + testValue2 + "\"]}";
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityProxy.getValueFromJsonArray(jsonObject, testKey1);
        assertEquals(testValue1, value);
    }

}
