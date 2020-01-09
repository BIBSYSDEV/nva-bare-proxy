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
import java.util.HashMap;
import java.util.Map;
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
    public static final String MY_MOCK_THROWS_AN_EXCEPTION = "my mock throws an exception";
    public static final String EMPTY_STRING = "";
    public static final String FAKE_FEIDE_ID = "bob@unit.no";
    public static final String FAKE_ORC_ID = "0000-0000-0000-0000";

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
        InputStream st = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(st);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ALL_PARAMETERS);
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("body", postRequestBody);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithFeideIdParam() throws Exception {
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(asStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_TWO_PARAMETERS);

        Map<String, Object> event = new HashMap<>();
        event.put("body", postRequestBody);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(asStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ONE_PARAMETER);

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("body", postRequestBody);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        String postResponseBody = this.readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        List<Authority> expectedResponseAuthority = new Gson().fromJson(postResponseBody, authorityListType);
        assertEquals(expectedResponseAuthority.get(0).getScn(), responseAuthority.get(0).getScn());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
        assertEquals(expectedResponseAuthority.get(0).getHandle(), responseAuthority.get(0).getHandle());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = this.readJsonStringFromFile(FETCH_AUTHORITY_EVENT_JSON_ONE_PARAMETER);

        Map<String, Object> event = new HashMap<>();
        event.put("body", postRequestBody);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
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
        String expectdExceptionMsg = "my mock throws an exception";
        when(mockBareConnection.connect(any())).thenThrow(new IOException(expectdExceptionMsg));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        event.put("body", postRequestBody);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(MY_MOCK_THROWS_AN_EXCEPTION));
    }

    @Test
    public void testNoBodyRequest() {
        Map<String, Object> event = new HashMap<>();
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        GatewayResponse result = fetchAuthorityHandler.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(FetchAuthorityHandler.MISSING_BODY));
    }

    @Test
    public void testSelectQueryParameterIsFeideId() {
        Authority authority = new Authority();
        authority.setName(EMPTY_STRING);
        authority.setFeideId(FAKE_FEIDE_ID);
        authority.setOrcId(FAKE_ORC_ID);
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        String queryParameter = fetchAuthorityHandler.selectQueryParameter(authority);
        assertEquals(FAKE_FEIDE_ID, queryParameter);
    }

    @Test
    public void testSelectQueryParameterIsOrcId() {
        Authority authority = new Authority();
        authority.setName(EMPTY_STRING);
        authority.setFeideId(EMPTY_STRING);
        authority.setOrcId(FAKE_ORC_ID);
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        String queryParameter = fetchAuthorityHandler.selectQueryParameter(authority);
        assertEquals(FAKE_ORC_ID, queryParameter);
    }

    @Test
    public void testSelectQueryParameter_Non() {
        Authority authority = new Authority();
        authority.setName(EMPTY_STRING);
        authority.setFeideId(EMPTY_STRING);
        authority.setOrcId(EMPTY_STRING);
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        String queryParameter = fetchAuthorityHandler.selectQueryParameter(authority);
        assertEquals(EMPTY_STRING, queryParameter);
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
