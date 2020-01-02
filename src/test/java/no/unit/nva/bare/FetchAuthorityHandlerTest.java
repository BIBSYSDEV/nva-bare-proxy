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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchAuthorityHandlerTest {

    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE = "/bareSingleAuthorityResponse.json";
    public static final String BARE_EMPTY_RESPONSE_JSON_FILE = "/bareEmptyResponse.json";
    public static final String FETCH_AUTHORITY_EVENT_JSON_ALL_PARAMETERS = "/fullFetchAuthorityEvent.json";
    public static final String FETCH_AUTHORITY_EVENT_JSON_TWO_PARAMETERS = "/twoParamsFetchAuthorityEvent.json";
    public static final String FETCH_AUTHORITY_EVENT_JSON_ONE_PARAMETER = "/oneParamFetchAuthorityEvent.json";
    public static final String SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "/singleAuthorityGatewayResponseBody.json";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccessfulResponseWithNameParam() throws Exception {
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        InputStream st = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(st);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ALL_PARAMETERS);
        GatewayResponse result = mockAuthorityProxy.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithFeideIdParam() throws Exception {
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        InputStream st = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(st);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_TWO_PARAMETERS);
        GatewayResponse result = mockAuthorityProxy.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream st = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(st);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ONE_PARAMETER);
        GatewayResponse result = (GatewayResponse) mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        List<Authority> expectedResponseAuthority = new Gson().fromJson(postResponseBody, authorityListType);
        assertEquals(expectedResponseAuthority.get(0).getScn(), responseAuthority.get(0).getScn());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ONE_PARAMETER);
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        assertTrue("The result should be an empty list", responseAuthority.isEmpty());
    }

    @Test
    public void testFailingRequest() throws Exception {
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ALL_PARAMETERS);
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        String expectdExceptionMsg = "my mock throws an exception";
        when(mockBareConnection.connect(any())).thenThrow(new IOException(expectdExceptionMsg));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, result.getStatus());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(expectdExceptionMsg));
    }

    @Test
    public void testSelectQueryParameter() {
        Authority authority = new Authority();
        authority.setName("");
        String feideId = "bob@unit.no";
        authority.setFeideId(feideId);
        authority.setOrcId("");
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        String queryParameter = fetchAuthorityHandler.selectQueryParameter(authority);
        assertEquals(feideId, queryParameter);
    }

    private String readJsonStringFromFile(String fetchAuthorityEventJsonAllParameters) {
        InputStream stream = FetchAuthorityHandlerTest.class.getResourceAsStream(fetchAuthorityEventJsonAllParameters);
        String postRequestBody;
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
            postRequestBody = scanner.useDelimiter("\\A").next();
        }
        return postRequestBody;
    }

}
