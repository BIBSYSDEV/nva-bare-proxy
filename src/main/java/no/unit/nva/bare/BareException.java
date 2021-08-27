package no.unit.nva.bare;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class BareException extends ApiGatewayException {

    public BareException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
