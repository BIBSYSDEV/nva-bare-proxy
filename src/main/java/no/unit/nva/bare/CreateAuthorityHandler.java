package no.unit.nva.bare;

import static nva.commons.core.attempt.Try.attempt;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Optional;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.StringUtils;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import nva.commons.core.exceptions.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function creating an authority in ARP.
 */
public class CreateAuthorityHandler extends ApiGatewayHandler<CreateAuthorityRequest, Authority> {

    public static final String COMMUNICATION_ERROR_WHILE_CREATING = "Communication failure while creating new "
                                                                    + "authority with name='%s'";
    public static final String CLIENT_MESSAGE_WHEN_REQUEST_TO_AUTHORITY_SERVER_FAILS = "Failed to create person "
                                                                                       + "authority";
    public static final String FAILED_RESPONSE = "Failed response:";
    public static final String INVALID_INPUT_ERROR_MESSAGE = "Invalid input:";
    private static final Logger logger = LoggerFactory.getLogger(CreateAuthorityHandler.class);
    protected final transient BareConnection bareConnection;

    public CreateAuthorityHandler() {
        this(new BareConnection(), new Environment());
    }

    public CreateAuthorityHandler(BareConnection bareConnection, Environment environment) {
        super(CreateAuthorityRequest.class, environment);
        this.bareConnection = bareConnection;
    }

    @Override
    protected Authority processInput(CreateAuthorityRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        validateInput(input);
        BareAuthority bareAuthority = createAuthorityOnBare(input.getInvertedName());
        return new AuthorityConverter(environment).asAuthority(bareAuthority);
    }

    @Override
    protected Integer getSuccessStatusCode(CreateAuthorityRequest input, Authority output) {
        return HttpURLConnection.HTTP_OK;
    }

    protected BareAuthority createAuthorityOnBare(String name) throws BadGatewayException {
        AuthorityConverter authorityConverter = new AuthorityConverter(environment);
        BareAuthority bareAuthority = authorityConverter.buildAuthority(name);

        HttpResponse<String> response = attempt(() -> bareConnection.createAuthority(bareAuthority))
            .orElseThrow(this::handleUnexpectedFailure);
        if (requestToBareSucceeded(response)) {
            return parseResponseFromBare(name, response);
        }
        throw handlerFailureResponse(response);
    }

    private void validateInput(CreateAuthorityRequest input) throws ApiGatewayException {
        Try.of(input)
            .map(CreateAuthorityRequest::validate)
            .orElseThrow(fail -> handlerInvalidRequest(fail, input));
    }

    private BadRequestException handlerInvalidRequest(Failure<CreateAuthorityRequest> fail,
                                                      CreateAuthorityRequest input) {
        return new BadRequestException(INVALID_INPUT_ERROR_MESSAGE + input, fail.getException());
    }

    private RuntimeException handleUnexpectedFailure(Failure<HttpResponse<String>> fail) {
        logger.error(ExceptionUtils.stackTraceInSingleLine(fail.getException()));
        return new RuntimeException(fail.getException());
    }

    private BadGatewayException handlerFailureResponse(HttpResponse<String> response) {
        logger.error(FAILED_RESPONSE + response.body());
        return new BadGatewayException(CLIENT_MESSAGE_WHEN_REQUEST_TO_AUTHORITY_SERVER_FAILS);
    }

    private BareAuthority parseResponseFromBare(String name, HttpResponse<String> response) throws BadGatewayException {
        return Optional.ofNullable(response)
            .map(HttpResponse::body)
            .filter(StringUtils::isNotBlank)
            .map(attempt(BareAuthority::fromJson))
            .flatMap(Try::toOptional)
            .orElseThrow(() -> emptyResponseFromServer(name));
    }

    private BadGatewayException emptyResponseFromServer(String inputName) {
        logger.error(String.format(COMMUNICATION_ERROR_WHILE_CREATING, inputName));
        return new BadGatewayException(COMMUNICATION_ERROR_WHILE_CREATING);
    }

    private boolean requestToBareSucceeded(HttpResponse<String> response) {
        return response.statusCode() == SC_CREATED || response.statusCode() == SC_OK;
    }
}
