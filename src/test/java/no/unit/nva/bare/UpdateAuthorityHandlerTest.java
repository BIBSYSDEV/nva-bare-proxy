package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAuthorityHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFailingRequestCauseEmptySCN() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("scn", "");
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
        pathParams.put("scn", "scn");
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
        pathParams.put("scn", "scn");
        requestEvent.setPathParameters(pathParams);
        requestEvent.setBody("{}");
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(UpdateAuthorityHandler.BODY_ARGS_MISSING);
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthoritySingleAuthorityResponse() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("scn", "scn");
        requestEvent.setPathParameters(pathParams);
        String mockFeideId = "foo.bar@unit.no";
        String mockOrcId = "0000-0000-0000-0000";
        requestEvent.setBody("{\"body\": \"{\\\"feideId\\\":\\\"" + mockFeideId + "\\\", \\\"orcId\\\":\\\"" + mockOrcId + "\\\"}\"}");
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream inputStream = UpdateAuthorityHandlerTest.class.getResourceAsStream("/bareSingleAuthorityResponse.json");
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(inputStream));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.OK, response.getStatus());
        assertEquals(mockFeideId, responseAuthority.getFeideId());
        assertEquals(mockOrcId, responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthorityNonAuthorityResponse() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("scn", "scn");
        requestEvent.setPathParameters(pathParams);
        String mockFeideId = "foo.bar@unit.no";
        String mockOrcId = "0000-0000-0000-0000";
        requestEvent.setBody("{\"body\": \"{\\\"feideId\\\":\\\"" + mockFeideId + "\\\", \\\"orcId\\\":\\\"" + mockOrcId + "\\\"}\"}");
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream inputStream = UpdateAuthorityHandlerTest.class.getResourceAsStream("/bareEmptyResponse.json");
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(inputStream));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
        assertEquals(Response.Status.NOT_FOUND, response.getStatus());
        assertEquals("", responseAuthority.getFeideId());
        assertEquals("", responseAuthority.getOrcId());
    }

    @Test
    public void testUpdateAuthorityManyAuthorityResponse() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        String mockScn = "scn";
        pathParams.put("scn", mockScn);
        requestEvent.setPathParameters(pathParams);
        String mockFeideId = "foo.bar@unit.no";
        String mockOrcId = "0000-0000-0000-0000";
        requestEvent.setBody("{\"body\": \"{\\\"feideId\\\":\\\"" + mockFeideId + "\\\", \\\"orcId\\\":\\\"" + mockOrcId + "\\\"}\"}");
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        InputStream inputStream = UpdateAuthorityHandlerTest.class.getResourceAsStream("/bareManyAuthorityResponse.json");
        when(mockBareConnection.connect(any())).thenReturn(new InputStreamReader(inputStream));
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.CONFLICT);
        expectedResponse.setErrorBody(String.format(UpdateAuthorityHandler.TO_MANY_AUTHORITIES_FOUND, mockScn));
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testUpdateAuthorityCommunicationErrors() throws IOException, URISyntaxException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        HashMap<String, String> pathParams = new HashMap<>();
        String mockScn = "scn";
        pathParams.put("scn", mockScn);
        requestEvent.setPathParameters(pathParams);
        String mockFeideId = "foo.bar@unit.no";
        String mockOrcId = "0000-0000-0000-0000";
        requestEvent.setBody("{\"body\": \"{\\\"feideId\\\":\\\"" + mockFeideId + "\\\", \\\"orcId\\\":\\\"" + mockOrcId + "\\\"}\"}");
        UpdateAuthorityHandler mockUpdateAuthorityHandler = new UpdateAuthorityHandler(mockBareConnection);
        String expectdExceptionMsg = "Exception is expected.";
        when(mockBareConnection.connect(any())).thenThrow(new IOException(expectdExceptionMsg));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        expectedResponse.setErrorBody(expectdExceptionMsg);
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testParseJsonBodyEvent() {
        String body = "{\"body\": \"{\\\"feideId\\\":\\\"foo.bar@unit.no\\\", \\\"orcId\\\":\\\"0000-0000-0000-0000\\\"}\"}";
        UpdateAuthorityHandler updateAuthorityHandler = new UpdateAuthorityHandler(new BareConnection());
        String feideId = updateAuthorityHandler.getValueFromJsonObject(body, UpdateAuthorityHandler.FEIDEID_KEY);
        assertEquals("foo.bar@unit.no", feideId);

    }

}
