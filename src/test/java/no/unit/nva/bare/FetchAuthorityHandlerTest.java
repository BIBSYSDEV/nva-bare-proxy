package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandlerTest.BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON;
import static no.unit.nva.bare.AuthorityConverterTest.HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH;
import static no.unit.nva.bare.FetchAuthorityHandler.ARPID_KEY;
import static no.unit.nva.bare.FetchAuthorityHandler.QUERY_STRING_PARAMETERS_KEY;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.unit.nva.testutils.TestHeaders;
import nva.commons.apigateway.ContentTypes;
import nva.commons.apigateway.HttpHeaders;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

public class FetchAuthorityHandlerTest {

    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE = "bareSingleAuthorityResponse.json";
    public static final String BARE_EMPTY_RESPONSE_JSON_FILE = "bareEmptyResponse.json";
    public static final String SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "singleAuthorityGatewayResponseBody.json";
    public static final String MY_MOCK_THROWS_AN_EXCEPTION = "my mock throws an exception";
    public static final String NAME_KEY = "name";
    public static final String FEIDEID_KEY = ValidIdentifierKey.FEIDEID.asString();
    public static final String ORCID_KEY = ValidIdentifierKey.ORCID.asString();
    public static final String SAMPLE_IDENTIFIER = "0000-1111-2222-3333";

    private BareConnection bareConnection;
    private Environment mockEnvironment;
    private HttpClient httpClient;

    /**
     * Initialise mocks and Config.
     */
    @BeforeEach
    public void setUp() {

        httpClient = mock(HttpClient.class);
        bareConnection = new BareConnection(httpClient);
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
            .thenReturn(HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH);
    }

    @Test
    public void testSuccessfulResponseWithNameParam() throws Exception {
        final String body = IoUtils.stringFromResources(Path.of(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE));
        whenSendingRequest().thenAnswer((invocation -> mockHttpResponse(body, HTTP_OK)));

        Map<String, Object> event = createEvent(NAME_KEY, "destroyer");

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_OK, result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), ContentTypes.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = IoUtils.stringFromResources(Path.of(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON));

        Authority expected = objectMapperWithEmpty.readValue(postResponseBody, new TypeReference<List<Authority>>() {
        }).get(0);
        Authority actual = objectMapperWithEmpty.readValue(content, new TypeReference<List<Authority>>() {
        }).get(0);
        assertEquals(expected, actual);
    }

    @Test
    public void handlerReturnsOkResponseWhenValidQueryParamArpIdProvided() throws Exception {

        String httpResponse = IoUtils.stringFromResources(Path.of(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON));
        whenSendingRequest().then(invocation -> mockHttpResponse(httpResponse, HTTP_OK));

        Map<String, Object> event = createEvent(ARPID_KEY, SAMPLE_IDENTIFIER);

        FetchAuthorityHandler handler = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = handler.handleRequest(event, null);
        assertEquals(HTTP_OK, result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), TestHeaders.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
    }

    @Test
    public void handlerReturnsInternalServerErrorResponseWhenErrorGettingAuthority() throws Exception {
        whenSendingRequest().thenThrow(new IOException(MY_MOCK_THROWS_AN_EXCEPTION));

        Map<String, Object> event = createEvent(ARPID_KEY, SAMPLE_IDENTIFIER);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_INTERNAL_ERROR, result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(MY_MOCK_THROWS_AN_EXCEPTION));
    }

    @Test
    public void testSuccessfulResponseWithFeideIdParam() throws Exception {
        String responseBody = IoUtils.stringFromResources(Paths.get(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE));
        whenSendingRequest().thenAnswer(invocation -> mockHttpResponse(responseBody, HTTP_OK));

        Map<String, Object> event = createEvent(FEIDEID_KEY, "sarah.serussi@unit.no");
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_OK, result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), TestHeaders.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);

        Authority expected = objectMapperWithEmpty.readValue(postResponseBody, new TypeReference<List<Authority>>() {
        }).get(0);
        Authority actual = objectMapperWithEmpty.readValue(content, new TypeReference<List<Authority>>() {
        }).get(0);
        assertEquals(expected, actual);
    }

    @Test
    public void testHandlerWithNull_QueryParams() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put(QUERY_STRING_PARAMETERS_KEY, null);
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_BAD_REQUEST, result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(FetchAuthorityHandler.MISSING_PARAMETERS));
    }

    @Test
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {

        String responseBody = IoUtils.stringFromResources(Paths.get(BARE_SINGLE_AUTHORITY_RESPONSE_JSON_FILE));
        whenSendingRequest().thenAnswer(invocation -> mockHttpResponse(responseBody, HTTP_OK));
        Map<String, Object> event = createEvent(ORCID_KEY, SAMPLE_IDENTIFIER);

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_OK, result.getStatusCode());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), TestHeaders.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);

        List<Authority> responseAuthority = objectMapperWithEmpty.readValue(content, new TypeReference<>() {
        });
        String postResponseBody = readJsonStringFromFile(SINGLE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        List<Authority> expectedResponseAuthority =
            objectMapperWithEmpty.readValue(postResponseBody,new TypeReference<>() {});

        assertEquals(expectedResponseAuthority.get(0).getSystemControlNumber(),
                     responseAuthority.get(0).getSystemControlNumber());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
        assertEquals(expectedResponseAuthority.get(0).getHandles(), responseAuthority.get(0).getHandles());
    }

    @Test
    public void testResponseWithoutQueryParams() {
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);

        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(null, null);
        assertEquals(HTTP_BAD_REQUEST, result.getStatusCode());

        Map<String, Object> event = new HashMap<>();
        result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_BAD_REQUEST, result.getStatusCode());

        Map<String, String> queryParameters = new HashMap<>();
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
        result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        String bareResponse = IoUtils.stringFromResources(Paths.get(BARE_EMPTY_RESPONSE_JSON_FILE));
        whenSendingRequest().thenAnswer(invocation -> mockHttpResponse(bareResponse, HTTP_OK));

        Map<String, Object> event = createEvent(FEIDEID_KEY, "sarha.suressi@unit.no");

        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_OK, result.getStatusCode());
        assertEquals(result.getHeaders().get(nva.commons.apigateway.HttpHeaders.CONTENT_TYPE),
                     TestHeaders.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);

        List<Authority> responseAuthority = objectMapperWithEmpty.readValue(content, new TypeReference<>() {
        });
        assertTrue(responseAuthority.isEmpty());
    }

    @Test
    public void testFailingRequest() throws Exception {
        whenSendingRequest().thenThrow(new IOException(MY_MOCK_THROWS_AN_EXCEPTION));

        Map<String, Object> event = createEvent(FEIDEID_KEY, "sarha.suressi@unit.no");
        FetchAuthorityHandler mockAuthorityProxy = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = mockAuthorityProxy.handleRequest(event, null);
        assertEquals(HTTP_INTERNAL_ERROR, result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(MY_MOCK_THROWS_AN_EXCEPTION));
    }

    @Test
    public void testNoBodyRequest() {
        Map<String, Object> event = new HashMap<>();
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler(bareConnection, mockEnvironment);
        CustomGatewayResponse result = fetchAuthorityHandler.handleRequest(event, null);
        assertEquals(HTTP_BAD_REQUEST, result.getStatusCode());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(FetchAuthorityHandler.MISSING_PARAMETERS));
    }

    protected static String readJsonStringFromFile(String fileName) {
        InputStream stream = IoUtils.inputStreamFromResources(Paths.get(fileName));
        return IoUtils.streamToString(stream);
    }

    private Map<String, Object> createEvent(String identifierType, String sampleIdentifier) {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(identifierType, sampleIdentifier);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParameters);
        return event;
    }

    private OngoingStubbing<HttpResponse> whenSendingRequest() throws IOException, InterruptedException {
        return when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)));
    }

    private HttpResponse<String> mockHttpResponse(String body, int statusCode) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.body()).thenReturn(body);
        when(response.statusCode()).thenReturn(statusCode);
        return response;
    }
}
