package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAuthorityHandlerTest {

    public static final String EMPTY_JSON = "{}";
    public static final String EMPTY_STRING = "";
    public static final String MOCK_SCN_VALUE = "scn";
    public static final String FULL_UPDATE_AUTHORITY_EVENT_JSON = "/fullUpdateAuthorityEvent.json";
    public static final String FOOBAR_UPDATE_AUTHORITY_EVENT_JSON = "/fooBarUpdateAuthorityEvent.json";
    public static final String ORCID_UPDATE_AUTHORITY_EVENT_JSON = "/orcidUpdateAuthorityEvent.json";
    public static final String FEIDEID_UPDATE_AUTHORITY_EVENT_JSON = "/feideidUpdateAuthorityEvent.json";
    public static final String EMPTY_UPDATE_AUTHORITY_EVENT_JSON = "{\"body\": { }}";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JSON = "/bareSingleAuthorityResponse.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JUST_ORCID_JSON =
            "/bareSingleAuthorityResponseJustOrcid.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JUST_FEIDEID_JSON =
            "/bareSingleAuthorityResponseJustFeideid.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityResponseWithAllIds.json";
    public static final String BARE_EMPTY_RESPONSE_JSON = "/bareEmptyResponse.json";
    public static final String BARE_MANY_AUTHORITY_RESPONSE_JSON = "/bareManyAuthorityResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";
    public static final String MOCK_FEIDE_VALUE = "foo.bar@unit.no";
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
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, EMPTY_STRING);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBody() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_BODY_ELEMENT_EVENT);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        UpdateAuthorityHandler updateAuthorityHandler = new UpdateAuthorityHandler();
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = updateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBodyParameters() {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        requestEvent.put(BODY_KEY, EMPTY_JSON);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityHandler.BODY_ARGS_MISSING);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponse() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FULL_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.update(any())).thenReturn(mockCloseableHttpResponse);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        String mockFeideId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.FEIDEID_KEY);
        String mockOrcId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.ORCID_KEY);
        assertEquals(mockFeideId, responseAuthority.getFeideId());
        assertEquals(mockOrcId, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponseOnlyFeideId() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(
                FEIDEID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JUST_FEIDEID_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.update(any())).thenReturn(mockCloseableHttpResponse);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        String mockFeideId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.FEIDEID_KEY);
        assertEquals(mockFeideId, responseAuthority.getFeideId());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponseOnlyOrcId() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(ORCID_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_RESPONSE_JUST_ORCID_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.update(any())).thenReturn(mockCloseableHttpResponse);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        String mockOrcId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.ORCID_KEY);
        assertEquals(mockOrcId, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponse_onlyOrcid()  {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        requestEvent.put(BODY_KEY, EMPTY_UPDATE_AUTHORITY_EVENT_JSON);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        mockCloseableHttpResponse.setEntity(mockEntity);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(UpdateAuthorityHandler.BODY_ARGS_MISSING);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityEmptyResponse() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FULL_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.update(any())).thenReturn(mockCloseableHttpResponse);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(String.format(UpdateAuthorityHandler.COMMUNICATION_ERROR_WHILE_UPDATING,
                MOCK_SCN_VALUE));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthorityBareConnectionError() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FULL_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockBareConnection.update(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        String content = response.getBody();
        assertNotNull(content);
        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
    }

    @Test
    public void testUpdateAuthorityNonAuthorityResponse() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream inputStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(inputStream));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode());
        assertEquals(EMPTY_STRING, responseAuthority.getFeideId());
        assertEquals(EMPTY_STRING, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthorityManyAuthorityResponse() throws Exception {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_MANY_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.CONFLICT.getStatusCode());
        expectedResponse.setErrorBody(String.format(UpdateAuthorityHandler.TO_MANY_AUTHORITIES_FOUND, MOCK_SCN_VALUE));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthorityCommunicationErrors() throws IOException, URISyntaxException {
        Map<String, Object> requestEvent = new HashMap<>();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.put(BODY_KEY, st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        when(mockBareConnection.connect(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testParseJsonBodyEvent() throws Exception {
        InputStream asStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String jsonString = IOUtils.toString(asStream, Charset.defaultCharset());
        UpdateAuthorityHandler updateAuthorityHandler = new UpdateAuthorityHandler(new BareConnection());
        String feideId = updateAuthorityHandler.getValueFromJsonObject(jsonString, UpdateAuthorityHandler.FEIDEID_KEY);
        assertEquals(MOCK_FEIDE_VALUE, feideId);

    }

}
