package no.unit.nva.bare;
import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InvalidInputException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_BAD_REQUEST;

    public InvalidInputException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
