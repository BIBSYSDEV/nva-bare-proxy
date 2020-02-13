package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAuthorityIdentifierHandlerTest {

    public static final String EMPTY_STRING = "";
    public static final String MOCK_SCN_VALUE = "scn";
    public static final String MOCK_ORGUNITID_VALUE = "orgunitid";
    public static final String MOCK_ORCID_VALUE = "orgunitid";
    public static final String MOCK_FEIDEID_VALUE = "feideid";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON = "/bareSingleAuthorityGetResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String BODY_KEY = "body";
    public static final String MOCK_BODY = "postRequestBody";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;
    @Mock
    CloseableHttpResponse mockCloseableHttpResponse;
    @Mock
    HttpEntity mockEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFailingRequestCauseEmptyPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptySCN() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, EMPTY_STRING);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingQualifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_QUALIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseInvalidQualifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY,
                ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.INVALID_VALUE_PATH_PARAMETER_QUALIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingIdentifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORGUNITID.asString());
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_IDENTIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingUpdatedIdentifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORGUNITID.asString());
        pathParams.put(UpdateAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORGUNITID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_UPDATED_IDENTIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthorityIdentifierFeideId() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.FEIDEID.asString());
        pathParams.put(UpdateAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_FEIDEID_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.UPDATED_IDENTIFIER_KEY, MOCK_FEIDEID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        InputStream stream1 =
                UpdateAuthorityIdentifierHandler.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.updateIdentifier(any(), any(), any(), any())).thenReturn(mockCloseableHttpResponse);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testResponseFromBareWhereStatusCodeBadRequest() throws IOException, URISyntaxException {
        UpdateAuthorityIdentifierHandler handler = new UpdateAuthorityIdentifierHandler(mockBareConnection);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.updateIdentifier(any(), any(), any(), any()))
                .thenReturn(mockCloseableHttpResponse);
        final GatewayResponse gatewayResponse = handler.updateIdentifier(MOCK_SCN_VALUE, "invalid",
                MOCK_FEIDEID_VALUE, MOCK_FEIDEID_VALUE);
        assertEquals(UpdateAuthorityIdentifierHandler.ERROR_CALLING_REMOTE_SERVER, gatewayResponse.getStatusCode());
    }

    @Test
    public void testUpdateAuthorityBareConnectionError() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
        pathParams.put(UpdateAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.UPDATED_IDENTIFIER_KEY, MOCK_FEIDEID_VALUE);

        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        when(mockBareConnection.updateIdentifier(any(), any(), any(), any())).thenThrow(
                new IOException(EXCEPTION_IS_EXPECTED));
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testUpdateAuthority_failingToReadAuthorityFromStream() throws Exception {
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockBareConnection.get(any())).thenReturn(null);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.updateIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                "may-britt.moser@ntnu.no", "may-britt.moser@ntnu.no"))
                .thenReturn(mockCloseableHttpResponse);
        GatewayResponse response =
                mockUpdateAuthorityHandler.updateIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                        "may-britt.moser@ntnu.no", "may-britt.moser@ntnu.no");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
    }

    @Test
    public void testUpdateAuthority_exceptionOnReadAuthorityFromBare() throws Exception {
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        mockCloseableHttpResponse.setEntity(mockEntity);

        when(mockBareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.updateIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                "may-britt.moser@ntnu.no", "may-britt.moser@ntnu.no"))
                .thenReturn(mockCloseableHttpResponse);
        GatewayResponse response =
                mockUpdateAuthorityHandler.updateIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                        "may-britt.moser@ntnu.no", "may-britt.moser@ntnu.no");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testUpdateAuthorityCommunicationErrors() throws IOException, URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
        pathParams.put(UpdateAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
        pathParams.put(UpdateAuthorityIdentifierHandler.UPDATED_IDENTIFIER_KEY, MOCK_ORCID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new UpdateAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.updateIdentifier(any(), any(), any(), any())).thenThrow(
                new IOException(EXCEPTION_IS_EXPECTED));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}