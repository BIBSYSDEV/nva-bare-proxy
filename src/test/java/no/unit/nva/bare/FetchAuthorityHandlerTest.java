package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static no.unit.nva.bare.FetchAuthorityHandler.ARPID_KEY;
import static no.unit.nva.bare.FetchAuthorityHandler.QUERY_STRING_PARAMETERS_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchAuthorityHandlerTest {

    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE = "/bareSingleAuthorityResponse.json";
    public static final String BARE_EMPTY_RESPONSE_JSON_FILE = "/bareEmptyResponse.json";
    public static final String SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "/singleAuthorityGatewayResponseBody.json";
    public static final String MY_MOCK_THROWS_AN_EXCEPTION = "my mock throws an exception";
    public static final String NAME_KEY = "name";
    public static final String FEIDEID_KEY = ValidIdentifierKey.FEIDEID.asString();
    public static final String ORCID_KEY = ValidIdentifierKey.ORCID.asString();
    public static final String SAMPLE_IDENTIFIER = "0000-1111-2222-3333";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    /**
     * Initialise mocks and Config.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final Config config = Config.getInstance();
        config.setBareHost(Config.BARE_HOST_KEY);
        config.setBareApikey(Config.BARE_APIKEY_KEY);
    }

    @Test
    public void testSuccessfulResponseWithNameParam() throws Exception {
        InputStream st = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(st);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        Map<String, Object> event = new HashMap<>();

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(NAME_KEY, "destroyer");
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void handlerReturnsOkResponseWhenValidQueryParamArpIdProvided() throws Exception {
        BareAuthority bareAuthority = new BareAuthority();
        bareAuthority.setSystemControlNumber(SAMPLE_IDENTIFIER);
        when(mockBareConnection.get(any())).thenReturn(bareAuthority);
        Map<String, Object> event = new HashMap<>();

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(ARPID_KEY, SAMPLE_IDENTIFIER);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(SAMPLE_IDENTIFIER));
    }

    @Test
    public void handlerReturnsInternalServerErrorResponseWhenErrorGettingAuthority() throws Exception {
        when(mockBareConnection.get(any())).thenThrow(new IOException(MY_MOCK_THROWS_AN_EXCEPTION));
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(ARPID_KEY, SAMPLE_IDENTIFIER);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(MY_MOCK_THROWS_AN_EXCEPTION));
    }

    @Test
    public void testSuccessfulResponseWithFeideIdParam() throws Exception {
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(asStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(FEIDEID_KEY, "sarah.serussi@unit.no");
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testHandlerWithNull_QueryParams() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put(QUERY_STRING_PARAMETERS_KEY, null);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(FetchAuthorityHandler.MISSING_PARAMETERS));
    }

    @Test
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(asStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        Map<String, Object> event = new HashMap<>();

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(ORCID_KEY, SAMPLE_IDENTIFIER);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        String postResponseBody = readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        List<Authority> expectedResponseAuthority = new Gson().fromJson(postResponseBody, authorityListType);
        assertEquals(expectedResponseAuthority.get(0).getSystemControlNumber(),
                responseAuthority.get(0).getSystemControlNumber());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
        assertEquals(expectedResponseAuthority.get(0).getHandles(), responseAuthority.get(0).getHandles());
    }

    @Test
    public void testResponseWithoutQueryParams()  {
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(mockBareConnection);
        GatewayResponse result = mockAuthorityProxy.handleRequest(null, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());

        Map<String, Object> event = new HashMap<>();
        result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());


        Map<String, String> queryParameters = new HashMap<>();
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
        result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON_FILE);
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        Map<String, Object> event = new HashMap<>();

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(FEIDEID_KEY, "sarha.suressi@unit.no");

        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);

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
        when(mockBareConnection.connect(any())).thenThrow(new IOException(MY_MOCK_THROWS_AN_EXCEPTION));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(FEIDEID_KEY, "sarha.suressi@unit.no");
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
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
        assertTrue(content.contains(FetchAuthorityHandler.MISSING_PARAMETERS));
    }


    protected static String readJsonStringFromFile(String fetchAuthorityEventJsonAllParameters) {
        InputStream stream = FetchAuthorityHandlerTest.class.getResourceAsStream(fetchAuthorityEventJsonAllParameters);
        String postRequestBody;
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
            postRequestBody = scanner.useDelimiter("\\A").next();
        }
        return postRequestBody;
    }

}
