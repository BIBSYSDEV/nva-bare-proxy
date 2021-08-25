package no.unit.nva.bare;

import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class BareCommunicationException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_INTERNAL_SERVER_ERROR;

    public BareCommunicationException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
