package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Handler for requests to Lambda function.
 */
public class DeleteAuthorityIdentifierHandler extends ApiGatewayHandler<DeleteAuthorityIdentifierRequest, String> {

    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String MISSING_PATH_PARAMETER_QUALIFIER = "Missing path parameter 'qualifier'.";
    public static final String INVALID_VALUE_PATH_PARAMETER_QUALIFIER = "Invalid path parameter 'qualifier'.";
    public static final String MISSING_REQUEST_JSON_BODY = "Missing json in body.";
    public static final String MISSING_ATTRIBUTE_IDENTIFIER = "Missing json attribute 'identifier'.";
    public static final String COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY =
            "Communication failure trying to update authority";
    public static final String SCN_KEY = "scn";
    public static final String QUALIFIER_KEY = "qualifier";
    public static final String EMPTY_STRING = "";
    public static final String REMOTE_SERVER_ERRORMESSAGE = "remote server errormessage: ";

    public static final List<String> VALID_QUALIFIERS = asList(ValidIdentifierKey.FEIDEID.asString(),
            ValidIdentifierKey.ORCID.asString(), ValidIdentifierKey.ORGUNITID.asString());

    private transient BareConnection bareConnection;

    /**
     * Default constructor for DeleteAuthorityIdentifierHandler.
     */
    @JacocoGenerated
    public DeleteAuthorityIdentifierHandler() {
        this(new Environment(), new BareConnection());
    }

    /**
     * Constructor for DeleteAuthorityIdentifierHandler.
     *
     * @param environment    environment
     * @param bareConnection bareConnection
     */
    public DeleteAuthorityIdentifierHandler(Environment environment, BareConnection bareConnection) {
        super(DeleteAuthorityIdentifierRequest.class, environment,
                LoggerFactory.getLogger(DeleteAuthorityIdentifierHandler.class));
        this.bareConnection = bareConnection;
    }

    @Override
    protected String processInput(DeleteAuthorityIdentifierRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateInput(input, requestInfo.getPathParameters());

        String scn = requestInfo.getPathParameters().get(SCN_KEY);
        String inputQualifier = requestInfo.getPathParameters().get(QUALIFIER_KEY);
        String qualifier = transformQualifier(inputQualifier);
        String identifier = input.getIdentifier();

        return deleteIdentifier(scn, qualifier, identifier);
    }


    private String transformQualifier(String inputQualifier) {
        if (inputQualifier.equals(ValidIdentifierKey.FEIDEID.asString())) {
            return ValidIdentifierSource.feide.asString();
        }
        return inputQualifier;
    }

    private void validateInput(DeleteAuthorityIdentifierRequest input, Map<String, String> pathParameters)
            throws InvalidInputException {
        if (StringUtils.isEmpty(pathParameters.get(SCN_KEY))) {
            throw new InvalidInputException(MISSING_PATH_PARAMETER_SCN);
        }
        if (StringUtils.isEmpty(pathParameters.get(QUALIFIER_KEY))) {
            throw new InvalidInputException(MISSING_PATH_PARAMETER_QUALIFIER);
        }
        if (!VALID_QUALIFIERS.contains(pathParameters.get(QUALIFIER_KEY))) {
            throw new InvalidInputException(INVALID_VALUE_PATH_PARAMETER_QUALIFIER);
        }
        if (Objects.isNull(input)) {
            throw new InvalidInputException(MISSING_REQUEST_JSON_BODY);
        }
        if (StringUtils.isEmpty(input.getIdentifier())) {
            throw new InvalidInputException(MISSING_ATTRIBUTE_IDENTIFIER);
        }
    }

    protected String deleteIdentifier(String scn, String qualifier, String identifier)
            throws ApiGatewayException {
        try {
            HttpResponse<String> response = bareConnection.deleteIdentifier(scn, qualifier, identifier);
            int responseCode = response.statusCode();
            logger.info("response (from bareConnection)=" + response);
            if (responseCode == SC_OK) {
                try {
                    final BareAuthority updatedAuthority = bareConnection.get(scn);
                    if (Objects.nonNull(updatedAuthority)) {
                        AuthorityConverter authorityConverter = new AuthorityConverter();
                        final Authority authority = authorityConverter.asAuthority(updatedAuthority);
                        return new Gson().toJson(authority);
                    } else {
                        logger.error(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
                        throw new BareCommunicationException(
                                COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
                    }
                } catch (IOException | URISyntaxException e) {
                    logger.error(e.getMessage(), e);
                    throw new BareException(e.getMessage());
                }
            } else {
                logger.error(String.format("deleteIdentifier - ErrorCode=%s, reasonPhrase=%s", response.statusCode(),
                        response.body()));
                throw new BareException(REMOTE_SERVER_ERRORMESSAGE + response.body());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new BareException(e.getMessage());
        }
    }

    @Override
    protected Integer getSuccessStatusCode(DeleteAuthorityIdentifierRequest input, String output) {
        return SC_OK;
    }
}
