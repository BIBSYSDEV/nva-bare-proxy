package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchAuthorityHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccessfulResponseWithNameParam() throws IOException {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"may-britt.moser@ntnu.no\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
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
    public void testSuccessfulResponseWithFeideIdParam() throws IOException {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"may-britt.moser@ntnu.no\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
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
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        String postResponseBody = "[\n"
                + "  {\n"
                + "    \"name\": \"Moser, May-Britt\",\n"
                + "    \"scn\": \"90517730\",\n"
                + "    \"feideId\": \"\",\n"
                + "    \"orcId\": \"\",\n"
                + "    \"birthDate\": \"1963-\"\n"
                + "  }\n"
                + "]";
        List<Authority> expectedResponseAuthority = new Gson().fromJson(postResponseBody, authorityListType);
        assertEquals(expectedResponseAuthority.get(0).getScn(), responseAuthority.get(0).getScn());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareEmptyResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        assertTrue("The result should be an empty list", responseAuthority.isEmpty());
    }

    @Test
    public void testFailingRequest() throws IOException {
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"\"\n"
                + "}";
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        String expectdExceptionMsg = "my mock throws an exception";
        when(mockBareConnection.connect(any())).thenThrow(new IOException(expectdExceptionMsg));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, result.getStatus());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(expectdExceptionMsg));
    }

    @Test
    public void testGetValueFromJson() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String testKey1 = "testKey1";
        String testValue1 = "testValue1";
        String testKey2 = "testKey2";
        String testValue2 = "testValue2";
        String testArray = "{\"" + testKey1 + "\": [\"" + testValue1 + "\"],"
                + "\"" + testKey2 + "\": [\"" + testValue2 + "\"]}";
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityConverter.getValueFromJsonArray(jsonObject, testKey1);
        assertEquals(testValue1, value);
    }

    @Test (expected = IOException.class)
    public void testExceptionOnBareConnection() throws IOException {
        BareConnection bareConnection = new BareConnection();
        URL emptUrl = new URL("http://iam.an.url");
        bareConnection.connect(emptUrl);
        fail();
    }

    @Test
    public void testErrorResponse() {
        String expectedJson = "{\"error\":\"error\"}";
        // calling real constructor (no need to mock as this is not talking to the internet)
        // but helps code coverage
        FetchAuthorityHandler fetchDoiMetadata = new FetchAuthorityHandler();
        String errorJson = fetchDoiMetadata.getErrorAsJson("error");
        assertEquals(expectedJson, errorJson);
    }

}
