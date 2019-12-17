package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

public class BareAuthorityHandler implements RequestHandler<APIGatewayProxyRequestEvent, GatewayResponse> {

    public static final String HTTP_METHOD_NOT_SUPPORTED = "HttpMethod not supported";

    @Override
    public GatewayResponse handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        String httpMethod = input.getHttpMethod();
        switch (httpMethod) {
            case HttpMethod.POST:
                FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
                gatewayResponse = fetchAuthorityHandler.handleRequest(input.getBody());
                break;
            case HttpMethod.PUT:
                UpdateAuthorityHandler updateAuthorityHandler = new UpdateAuthorityHandler();
                gatewayResponse = updateAuthorityHandler.handleRequest(input);
                break;
            default:
                gatewayResponse.setErrorBody(HTTP_METHOD_NOT_SUPPORTED);
                gatewayResponse.setStatus(Response.Status.METHOD_NOT_ALLOWED);
        }
        return gatewayResponse;
    }

}
