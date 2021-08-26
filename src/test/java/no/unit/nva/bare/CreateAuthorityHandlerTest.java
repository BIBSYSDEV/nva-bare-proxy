package no.unit.nva.bare;

import static no.unit.nva.bare.AuthorityConverterTest.HTTPS_LOCALHOST_PERSON;
import static no.unit.nva.bare.CreateAuthorityRequest.MALFORMED_NAME_VALUE;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static nva.commons.core.attempt.Try.attempt;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private static final Context CONTEXT = mock(Context.class);
    private BareConnection mockBareConnection;
    private HttpResponse mockHttpResponse;
    private Environment mockEnvironment;
    private ByteArrayOutputStream outputStream;

    /**
     * Initialize test environment.
     */

    @BeforeEach
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        mockHttpResponse = mock(HttpResponse.class);
        mockBareConnection = mock(BareConnection.class);
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
            .thenReturn(HTTPS_LOCALHOST_PERSON);
        when(mockEnvironment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
    }

    @Test
    public void testCreateAuthority() throws IOException, URISyntaxException, InterruptedException {

        InputStream is = IoUtils.inputStreamFromResources(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE);
        final String mockBody = IoUtils.streamToString(is);

        when(mockHttpResponse.statusCode()).thenReturn(SC_CREATED);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);

        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        createAuthorityHandler.handleRequest(sampleRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);
        assertEquals(SC_OK, response.getStatusCode());
        String resp = FetchAuthorityHandlerTest.readJsonStringFromFile(CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        Authority expected = objectMapperWithEmpty.readValue(resp, Authority.class);
        Authority actual = objectMapperWithEmpty.readValue(response.getBody(), Authority.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateAuthority_FailingToReadAuthorityFromResponseStream() throws IOException, URISyntaxException,
                                                                                      InterruptedException {
        final String emptyResponse = EMPTY_BODY;

        when(mockHttpResponse.statusCode()).thenReturn(SC_CREATED);
        when(mockHttpResponse.body()).thenReturn(emptyResponse);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);

        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        createAuthorityHandler.handleRequest(sampleRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    public void testCreateAuthority_FailingToCreateAuthorityOnBare() throws IOException, URISyntaxException,
                                                                            InterruptedException {
        final TestAppender logger = LogUtils.getTestingAppenderForRootLogger();
        when(mockHttpResponse.statusCode()).thenReturn(SC_NOT_ACCEPTABLE);
        when(mockHttpResponse.body()).thenReturn(MOCK_ERROR_MESSAGE);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);

        createAuthorityHandler.handleRequest(sampleRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, response.getStatusCode());
        assertThat(logger.getMessages(), containsString(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthority_ExceptionFromBare() throws IOException, URISyntaxException, InterruptedException {
        final TestAppender logger = LogUtils.getTestingAppenderForRootLogger();
        when(mockBareConnection.createAuthority(any())).thenThrow(new IOException(MOCK_ERROR_MESSAGE));
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        createAuthorityHandler.handleRequest(sampleRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(SC_INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertThat(logger.getMessages(), containsString(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthorityMissingBodyParam_Name() throws IOException {
        final TestAppender logger = LogUtils.getTestingAppenderForRootLogger();
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection, mockEnvironment);
        createAuthorityHandler.handleRequest(emptyRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertThat(logger.getMessages(), containsString(CreateAuthorityHandler.INVALID_INPUT_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthorityNotInvertedBodyParam_Name() throws IOException {
        final TestAppender logger = LogUtils.getTestingAppenderForRootLogger();
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        createAuthorityHandler.handleRequest(notInvertedName(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(SC_BAD_REQUEST, response.getStatusCode());
        assertThat(logger.getMessages(), containsString(MALFORMED_NAME_VALUE));
    }

    @Test
    public void testCreateAuthorityMissingEvent() throws IOException {

        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        createAuthorityHandler.handleRequest(emptyRequest(), outputStream, CONTEXT);
        GatewayResponse<Authority> response = GatewayResponse.fromOutputStream(outputStream);

        assertEquals(SC_BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.INVALID_INPUT_ERROR_MESSAGE));
    }

    private InputStream notInvertedName() throws JsonProcessingException {
        return new HandlerRequestBuilder<CreateAuthorityRequest>(objectMapperWithEmpty)
            .withBody(new CreateAuthorityRequest("name without comma"))
            .build();
    }

    private InputStream sampleRequest() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        String jsonString = attempt(() -> objectMapperWithEmpty.writeValueAsString(requestEvent)).orElseThrow();
        return IoUtils.stringToStream(jsonString);
    }

    private InputStream emptyRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<CreateAuthorityRequest>(objectMapperWithEmpty).build();
    }
}
