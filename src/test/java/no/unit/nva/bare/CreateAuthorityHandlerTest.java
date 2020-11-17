package no.unit.nva.bare;

import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static no.unit.nva.bare.AuthorityConverterTest.HTTPS_LOCALHOST_PERSON;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateAuthorityHandlerTest {

    public static final String EMPTY_BODY = "";
    public static final String BODY_KEY = "body";
    public static final String MOCK_NAME = "Unit, DotNo";
    public static final String MOCK_BODY = "{\"invertedname\": \"" + MOCK_NAME + "\"}";
    public static final String MOCK_BODY_NOT_INVERTED = "{\"invertedname\": \"no comma\"}";
    public static final String MOCK_BODY_NONAME = "{\"noname\": \"" + MOCK_NAME + "\"}";
    public static final String CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "createAuthorityGatewayResponseBody.json";
    public static final String BARE_SINGLE_AUTHORITY_CREATE_RESPONSE = "bareSingleAuthorityCreateResponse.json";
    public static final String MOCK_ERROR_MESSAGE = "I want to fail";


    private BareConnection mockBareConnection;
    private HttpResponse mockHttpResponse;
    private Environment mockEnvironment;
    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    /**
     * Initialize test environment.
     */
    @BeforeEach
    public void setUp() {
        mockHttpResponse = mock(HttpResponse.class);
        mockBareConnection = mock(BareConnection.class);
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
                .thenReturn(HTTPS_LOCALHOST_PERSON);

    }

    @Test
    public void testCreateAuthority() throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
//        InputStream responseStream =
//                CreateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE);
//        final String mockBody = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
        InputStream is = IoUtils.inputStreamFromResources(Paths.get(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE));
        final String mockBody = IoUtils.streamToString(is);


        when(mockHttpResponse.statusCode()).thenReturn(SC_CREATED);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_OK, response.getStatusCode());
        String resp = FetchAuthorityHandlerTest.readJsonStringFromFile(CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        Authority expected = mapper.readValue(resp, Authority.class);
        Authority actual = mapper.readValue(response.getBody(), Authority.class);
        assertEquals(expected,actual);
    }

    @Test
    public void testCreateAuthority_FailingToReadAuthorityFromResponseStream() throws IOException, URISyntaxException,
            InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
//        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        final String mockBody = EMPTY_BODY; // IOUtils.toString(emptyStream, StandardCharsets.UTF_8);

        when(mockHttpResponse.statusCode()).thenReturn(SC_CREATED);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);

        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_INTERNAL_SERVER_ERROR, response.getStatusCode());
//        assertTrue(response.getBody().contains(
//                String.format(CreateAuthorityHandler.COMMUNICATION_ERROR_WHILE_CREATING, MOCK_NAME)));
    }

    @Test
    public void testCreateAuthority_FailingToCreateAuthorityOnBare() throws IOException, URISyntaxException,
            InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        when(mockHttpResponse.statusCode()).thenReturn(SC_NOT_ACCEPTABLE);
        when(mockHttpResponse.body()).thenReturn(MOCK_ERROR_MESSAGE);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthority_ExceptionFromBare() throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        when(mockBareConnection.createAuthority(any())).thenThrow(new IOException(MOCK_ERROR_MESSAGE));
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthorityMissingBodyParam_Name() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY_NONAME);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.BODY_ARGS_MISSING));
    }

    @Test
    public void testCreateAuthorityNotInvertedBodyParam_Name() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY_NOT_INVERTED);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.MALFORMED_NAME_VALUE));
    }

    @Test
    public void testCreateAuthorityMissingEvent() {
        Map<String, Object> requestEvent = new HashMap<>();
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(SC_BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.MISSING_EVENT_ELEMENT_BODY));
    }
}
