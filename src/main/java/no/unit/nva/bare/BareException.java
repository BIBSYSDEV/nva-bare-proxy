package no.unit.nva.bare;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class BareException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_INTERNAL_SERVER_ERROR;

    public BareException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
