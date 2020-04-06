package no.unit.nva.bare;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateAuthorityHandlerTest {

    public static final String BODY_KEY = "body";
    public static final String MOCK_NAME = "Unit, DotNo";
    public static final String MOCK_BODY = "{\"invertedname\": \"" + MOCK_NAME + "\"}";
    public static final String MOCK_BODY_NOT_INVERTED = "{\"invertedname\": \"no comma\"}";
    public static final String MOCK_BODY_NONAME = "{\"noname\": \"" + MOCK_NAME + "\"}";
    public static final String CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "/createAuthorityGatewayResponseBody.json";
    public static final String BARE_SINGLE_AUTHORITY_CREATE_RESPONSE = "/bareSingleAuthorityCreateResponse.json";
    public static final String MOCK_ERROR_MESSAGE = "I want to fail";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;
    @Mock
    HttpResponse mockHttpResponse;

    @Test
    public void testCreateAuthority() throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        InputStream responseStream =
                CreateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE);
        final String mockBody = IOUtils.toString(responseStream, StandardCharsets.UTF_8);

        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        String resp = FetchAuthorityHandlerTest.readJsonStringFromFile(CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testCreateAuthority_FailingToReadAuthorityFromResponseStream() throws IOException, URISyntaxException,
            InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        final String mockBody = IOUtils.toString(emptyStream, StandardCharsets.UTF_8);

        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);

        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(
                String.format(CreateAuthorityHandler.COMMUNICATION_ERROR_WHILE_CREATING, MOCK_NAME)));
    }

    @Test
    public void testCreateAuthority_FailingToCreateAuthorityOnBare() throws IOException, URISyntaxException,
            InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.NOT_ACCEPTABLE.getStatusCode());
        when(mockHttpResponse.body()).thenReturn(MOCK_ERROR_MESSAGE);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthority_ExceptionFromBare() throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        when(mockBareConnection.createAuthority(any())).thenThrow(new IOException(MOCK_ERROR_MESSAGE));
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthorityMissingBodyParam_Name() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY_NONAME);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.BODY_ARGS_MISSING));
    }

    @Test
    public void testCreateAuthorityNotInvertedBodyParam_Name() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY_NOT_INVERTED);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.MALFORMED_NAME_VALUE));
    }

    @Test
    public void testCreateAuthorityMissingEvent() {
        Map<String, Object> requestEvent = new HashMap<>();
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.MISSING_EVENT_ELEMENT_BODY));
    }
}
