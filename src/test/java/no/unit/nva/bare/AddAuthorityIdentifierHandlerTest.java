package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
public class AddAuthorityIdentifierHandlerTest {

    public static final String EMPTY_JSON = "{}";
    public static final String EMPTY_STRING = "";
    public static final String MOCK_SCN_VALUE = "scn";
    public static final String FOOBAR_UPDATE_AUTHORITY_EVENT_JSON = "/fooBarUpdateAuthorityEvent.json";
    public static final String ORCID_UPDATE_AUTHORITY_EVENT_JSON = "/orcidUpdateAuthorityEvent.json";
    public static final String ORGUNITID_UPDATE_AUTHORITY_EVENT_JSON = "/orgunitidUpdateAuthorityEvent.json";
    public static final String FEIDEID_UPDATE_AUTHORITY_EVENT_JSON = "/feideidUpdateAuthorityEvent.json";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String EMPTY_UPDATE_AUTHORITY_EVENT_JSON = "{\"body\": { }}";
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
    public void testFailingRequestCauseEmptySCN() {
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(BODY_KEY, MOCK_BODY);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, EMPTY_STRING);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(AddAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBody() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(AddAuthorityIdentifierHandler.MISSING_EVENT_ELEMENT_BODY);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        AddAuthorityIdentifierHandler updateAuthorityHandler = new AddAuthorityIdentifierHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(AddAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = updateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBodyParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        requestEvent.put(BODY_KEY, EMPTY_JSON);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(AddAuthorityIdentifierHandler.BODY_ARGS_MISSING);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }


    @Test
    public void testUpdateAuthoritySingleAuthorityResponseOnlyFeideId() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                FEIDEID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);


        InputStream stream1 =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.addIdentifier(any(), any())).thenReturn(mockCloseableHttpResponse);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        responseAuthority.getName();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponseOnlyOrcId() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                ORCID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);


        InputStream stream1 =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.addIdentifier(any(), any())).thenReturn(mockCloseableHttpResponse);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponseOnlyOrgUnitId() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                ORGUNITID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);


        InputStream stream1 =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.addIdentifier(any(), any())).thenReturn(mockCloseableHttpResponse);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponse_onlyOrcid() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        requestEvent.put(BODY_KEY, EMPTY_UPDATE_AUTHORITY_EVENT_JSON);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        mockCloseableHttpResponse.setEntity(mockEntity);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(AddAuthorityIdentifierHandler.BODY_ARGS_MISSING);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }


    @Test
    public void testUpdateAuthorityBareConnectionError() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(ORCID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);

        InputStream stream1 =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
        when(mockBareConnection.get(any())).thenReturn(bareAuthority);
        when(mockBareConnection.addIdentifier(any(), any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testUpdateAuthorityCommunicationErrors() throws IOException, URISyntaxException {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(AddAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        AddAuthorityIdentifierHandler mockUpdateAuthorityHandler =
                new AddAuthorityIdentifierHandler(mockBareConnection);
        when(mockBareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testResponseFromBareWhereStatusCodeBadRequest() throws IOException, URISyntaxException {
        AddAuthorityIdentifierHandler handler = new AddAuthorityIdentifierHandler(mockBareConnection);
        AuthorityIdentifier authorityIdentifier =
                new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "feide");


        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBareConnection.addIdentifier(MOCK_SCN_VALUE, authorityIdentifier))
                .thenReturn(mockCloseableHttpResponse);
        final GatewayResponse gatewayResponse = handler.updateAuthorityOnBare(MOCK_SCN_VALUE, authorityIdentifier);
        assertEquals(AddAuthorityIdentifierHandler.ERROR_CALLING_REMOTE_SERVER, gatewayResponse.getStatusCode());
    }

    @Test
    public void testEmptyResponseFromBare() throws IOException, URISyntaxException {
        AddAuthorityIdentifierHandler handler = new AddAuthorityIdentifierHandler(mockBareConnection);
        AuthorityIdentifier authorityIdentifier =
                new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "feide");

        StringReader reader = new StringReader(EMPTY_STRING);
        InputStream fakeStream = new ReaderInputStream(reader, Charset.defaultCharset());

        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(fakeStream), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);

        final GatewayResponse gatewayResponse = handler.addIdentifier(MOCK_SCN_VALUE, authorityIdentifier);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), gatewayResponse.getStatusCode());
    }

    @Test
    public void testTryToAddExistingIdentifier() throws IOException, URISyntaxException {
        AddAuthorityIdentifierHandler handler = new AddAuthorityIdentifierHandler(mockBareConnection);
        InputStream streamResp = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);

        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(streamResp), BareAuthority.class);
        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);


        AuthorityIdentifier authorityIdentifier =
                new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "may-britt.moser@ntnu.no");

        final GatewayResponse gatewayResponse = handler.addIdentifier(MOCK_SCN_VALUE, authorityIdentifier);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), gatewayResponse.getStatusCode());
    }

}
