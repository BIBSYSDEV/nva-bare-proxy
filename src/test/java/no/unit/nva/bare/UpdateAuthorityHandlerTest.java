package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
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
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_JSON = "/bareSingleAuthorityResponse.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON = "/bareSingleAuthorityResponseWithAllIds.json";
    public static final String BARE_EMPTY_RESPONSE_JSON = "/bareEmptyResponse.json";
    public static final String BARE_MANY_AUTHORITY_RESPONSE_JSON = "/bareManyAuthorityResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";
    public static final String MOCK_FEIDE_VALUE = "foo.bar@unit.no";

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
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, EMPTY_STRING);
        requestEvent.setPathParameters(pathParams);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_PATH_PARAMETER_SCN);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBody() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_BODY_ELEMENT_EVENT);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testFailingRequestCauseEmptyBodyParameters() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        requestEvent.setBody(EMPTY_JSON);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(UpdateAuthorityHandler.BODY_ARGS_MISSING);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponse() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(FULL_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.setBody(st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockBareConnection.update(any())).thenReturn(mockCloseableHttpResponse);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK, response.getStatus());
        String mockFeideId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.FEIDEID_KEY);
        String mockOrcId = mockUpdateAuthorityHandler.getValueFromJsonObject(st, UpdateAuthorityHandler.ORCID_KEY);
        assertEquals(mockFeideId, responseAuthority.getFeideId());
        assertEquals(mockOrcId, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthorityNonAuthorityResponse() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.setBody(st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream inputStream = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_EMPTY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(inputStream));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.NOT_FOUND, response.getStatus());
        assertEquals(EMPTY_STRING, responseAuthority.getFeideId());
        assertEquals(EMPTY_STRING, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthorityManyAuthorityResponse() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.setBody(st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream stream1 = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_MANY_AUTHORITY_RESPONSE_JSON);
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(stream1));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.CONFLICT);
        expectedResponse.setErrorBody(String.format(UpdateAuthorityHandler.TO_MANY_AUTHORITIES_FOUND, MOCK_SCN_VALUE));
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthorityCommunicationErrors() throws IOException, URISyntaxException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(UpdateAuthorityHandler.SCN_KEY, MOCK_SCN_VALUE);
        requestEvent.setPathParameters(pathParams);
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String st = IOUtils.toString(asStream, Charset.defaultCharset());
        requestEvent.setBody(st);
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        when(mockBareConnection.connect(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testParseJsonBodyEvent() throws Exception {
        InputStream asStream = FetchAuthorityHandlerTest.class.getResourceAsStream(FOOBAR_UPDATE_AUTHORITY_EVENT_JSON);
        String jsonString = IOUtils.toString(asStream, Charset.defaultCharset());
        UpdateAuthorityHandler updateAuthorityHandler = new UpdateAuthorityHandler(new BareConnection());
        String feideId = updateAuthorityHandler.getValueFromJsonObject(jsonString, UpdateAuthorityHandler.FEIDEID_KEY);
        assertEquals(MOCK_FEIDE_VALUE, feideId);

    }

}
