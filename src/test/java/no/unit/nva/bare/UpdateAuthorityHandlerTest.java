package no.unit.nva.bare;


import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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
import java.io.ObjectStreamClass;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
}
