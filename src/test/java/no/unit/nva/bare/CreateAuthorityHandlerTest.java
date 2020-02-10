package no.unit.nva.bare;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateAuthorityHandlerTest {

    public static final String BODY_KEY = "body";
    public static final String MOCK_NAME = "Unit, DotNo";
    public static final String MOCK_BODY = "{\"invertedname\": \"" + MOCK_NAME + "\"}";
    public static final String MOCK_BODY_NONAME = "{\"noname\": \"" + MOCK_NAME + "\"}";
    public static final String CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON = "/createAuthorityGatewayResponseBody.json";
    public static final String BARE_SINGLE_AUTHORITY_CREATE_RESPONSE = "/bareSingleAuthorityCreateResponse.json";
    public static final String MOCK_ERROR_MESSAGE = "I want to fail";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;
    @Mock
    CloseableHttpResponse mockCloseableHttpResponse;
    @Mock
    HttpEntity mockEntity;

    @Test
    public void testCreateAuthority() throws IOException, URISyntaxException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockCloseableHttpResponse);
        InputStream responseStream =
                CreateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE);
        when(mockEntity.getContent()).thenReturn(responseStream);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        String resp = FetchAuthorityHandlerTest.readJsonStringFromFile(CREATE_AUTHORITY_GATEWAY_RESPONSE_BODY_JSON);
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testCreateAuthority_FailingToReadAuthorityFromResponseStream() throws IOException, URISyntaxException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockCloseableHttpResponse);
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);;
        when(mockEntity.getContent()).thenReturn(emptyStream);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(
                String.format(CreateAuthorityHandler.COMMUNICATION_ERROR_WHILE_CREATING, MOCK_NAME)));
    }

    @Test
    public void testCreateAuthority_FailingToCreateAuthorityOnBare() throws IOException, URISyntaxException {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.NOT_ACCEPTABLE.getStatusCode());
        when(mockStatusLine.getReasonPhrase()).thenReturn(MOCK_ERROR_MESSAGE);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.createAuthority(any())).thenReturn(mockCloseableHttpResponse);
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler(mockBareConnection);
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(MOCK_ERROR_MESSAGE));
    }

    @Test
    public void testCreateAuthority_ExceptionFromBare() throws IOException, URISyntaxException {
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
    public void testCreateAuthorityMissingEvent() {
        Map<String, Object> requestEvent = new HashMap<>();
        CreateAuthorityHandler createAuthorityHandler = new CreateAuthorityHandler();
        final GatewayResponse response = createAuthorityHandler.handleRequest(requestEvent, null);
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertTrue(response.getBody().contains(CreateAuthorityHandler.MISSING_EVENT_ELEMENT_BODY));
    }
}
