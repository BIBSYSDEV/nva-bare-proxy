package no.unit.nva.bare;

import com.google.gson.Gson;
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
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteAuthorityIdentifierHandlerTest {

    public static final String EMPTY_STRING = "";
    public static final String MOCK_SCN_VALUE = "scn";
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
    HttpResponse mockHttpResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFailingRequestCauseEmptyPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptySCN() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, EMPTY_STRING);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingQualifier() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_QUALIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseInvalidQualifier() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY,
                ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DeleteAuthorityIdentifierHandler.INVALID_VALUE_PATH_PARAMETER_QUALIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingIdentifier() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORGUNITID.asString());
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_IDENTIFIER);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testDeleteAuthorityIdentifierFeideId() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.FEIDEID.asString());
        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_FEIDEID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        InputStream stream1 =
                DeleteAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenReturn(mockHttpResponse);
        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
                new DeleteAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockDeleteAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testResponseFromBareWhereStatusCodeBadRequest() throws IOException, URISyntaxException,
            InterruptedException {
        DeleteAuthorityIdentifierHandler handler = new DeleteAuthorityIdentifierHandler(mockBareConnection);
        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(mockBareConnection.deleteIdentifier(any(), any(), any()))
                .thenReturn(mockHttpResponse);
        final GatewayResponse gatewayResponse = handler.deleteIdentifier(MOCK_SCN_VALUE, "invalid",
                MOCK_FEIDEID_VALUE);
        assertEquals(DeleteAuthorityIdentifierHandler.ERROR_CALLING_REMOTE_SERVER, gatewayResponse.getStatusCode());
    }

    @Test
    public void testDeleteAuthorityBareConnectionError() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenThrow(
                new IOException(EXCEPTION_IS_EXPECTED));
        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new DeleteAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testDeleteAuthority_failingToReadAuthorityFromStream() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockBareConnection.get(any())).thenReturn(null);
        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
                new DeleteAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                "may-britt.moser@ntnu.no"))
                .thenReturn(mockHttpResponse);
        GatewayResponse response =
                mockDeleteAuthorityHandler.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                        "may-britt.moser@ntnu.no");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
    }

    @Test
    public void testDeleteAuthority_exceptionOnReadAuthorityFromBare() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());

        when(mockBareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
                new DeleteAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                "may-britt.moser@ntnu.no"))
                .thenReturn(mockHttpResponse);
        GatewayResponse response =
                mockDeleteAuthorityHandler.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
                        "may-britt.moser@ntnu.no");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testDeleteAuthorityCommunicationErrors() throws IOException, URISyntaxException, InterruptedException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
                new DeleteAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenThrow(
                new IOException(EXCEPTION_IS_EXPECTED));
        GatewayResponse response = mockDeleteAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
