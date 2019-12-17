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
import static org.junit.Assert.assertEquals;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

@RunWith(MockitoJUnitRunner.class)
public class BareAuthorityHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleRequestPosting() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.POST);
        BareAuthorityHandler bareAuthorityHandler = new BareAuthorityHandler();
        GatewayResponse gatewayResponse = bareAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(String.format(FetchAuthorityHandler.MISSING_BODY_ELEMENTS, null));
        assertEquals(expectedResponse.getStatus(), gatewayResponse.getStatus());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testHandleRequestPuting() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.PUT);
        BareAuthorityHandler bareAuthorityHandler = new BareAuthorityHandler();
        GatewayResponse gatewayResponse = bareAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.BAD_REQUEST);
        expectedResponse.setErrorBody(UpdateAuthorityHandler.MISSING_PATH_PARAMETER_SCN);
        assertEquals(expectedResponse.getStatus(), gatewayResponse.getStatus());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testHandleRequestNotSupportedMethod() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.GET);
        BareAuthorityHandler bareAuthorityHandler = new BareAuthorityHandler();
        GatewayResponse gatewayResponse = bareAuthorityHandler.handleRequest(requestEvent, null);
        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatus(Response.Status.METHOD_NOT_ALLOWED);
        expectedResponse.setErrorBody(BareAuthorityHandler.HTTP_METHOD_NOT_SUPPORTED);
        assertEquals(expectedResponse.getStatus(), gatewayResponse.getStatus());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }
}
