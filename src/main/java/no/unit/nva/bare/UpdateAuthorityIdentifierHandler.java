package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonCreator;
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
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Handler for requests to Lambda function.
 */
public class UpdateAuthorityIdentifierHandler extends ApiGatewayHandler<UpdateAuthorityIdentifierRequest, Authority> {

    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String MISSING_PATH_PARAMETER_QUALIFIER = "Missing path parameter 'qualifier'.";
    public static final String INVALID_VALUE_PATH_PARAMETER_QUALIFIER = "Invalid path parameter 'qualifier'.";
    public static final String MISSING_REQUEST_JSON_BODY = "Missing json in body.";
    public static final String MISSING_ATTRIBUTE_IDENTIFIER = "Missing json attribute 'identifier'.";
    public static final String MISSING_ATTRIBUTE_UPDATED_IDENTIFIER =
            "Missing json attribute 'updatedIdentifier'.";
    public static final String COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY =
            "Communication failure trying to update authority";
    public static final String SCN_KEY = "scn";
    public static final String QUALIFIER_KEY = "qualifier";
    public static final String REMOTE_SERVER_ERRORMESSAGE = "remote server errormessage: ";

    public static final List<String> VALID_QUALIFIERS = asList(
            ValidIdentifierKey.FEIDEID.asString(),
            ValidIdentifierKey.ORCID.asString(),
            ValidIdentifierKey.ORGUNITID.asString());

    private final transient BareConnection bareConnection;


    /**
     * Default constructor for UpdateAuthorityIdentifierHandler.
     */
    @JacocoGenerated
    @JsonCreator
    public UpdateAuthorityIdentifierHandler() {
        this(new Environment(), new BareConnection());
    }

    /**
     * Constructor for UpdateAuthorityIdentifierHandler.
     *
     * @param environment    environment
     * @param bareConnection bareConnection
     */
    public UpdateAuthorityIdentifierHandler(Environment environment, BareConnection bareConnection) {
        super(UpdateAuthorityIdentifierRequest.class, environment,
                LoggerFactory.getLogger(UpdateAuthorityIdentifierHandler.class));
        this.bareConnection = bareConnection;
    }

    @Override
    protected Authority processInput(UpdateAuthorityIdentifierRequest input, RequestInfo requestInfo,
                                     Context context) throws ApiGatewayException {

        validateInput(input, requestInfo.getPathParameters());

        String scn = requestInfo.getPathParameter(SCN_KEY);
        String inputQualifier = requestInfo.getPathParameter(QUALIFIER_KEY);
        String qualifier = transformQualifier(inputQualifier);
        String identifier = input.getIdentifier();
        String updatedIdentifier = input.getUpdatedIdentifier();

        return updateIdentifier(scn, qualifier, identifier, updatedIdentifier);
    }

    private String transformQualifier(String inputQualifier) {
        if (inputQualifier.equals(ValidIdentifierKey.FEIDEID.asString())) {
            return ValidIdentifierSource.feide.asString();
        }
        return inputQualifier;
    }

    private void validateInput(UpdateAuthorityIdentifierRequest input, Map<String, String> pathParameters)
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
        if (StringUtils.isEmpty(input.getUpdatedIdentifier())) {
            throw new InvalidInputException(MISSING_ATTRIBUTE_UPDATED_IDENTIFIER);
        }
    }

    @JacocoGenerated
    protected Authority updateIdentifier(String scn, String qualifier, String identifier,
                                         String updatedIdentifier) throws BareCommunicationException, BareException {

        try {
            HttpResponse<String> response = bareConnection.updateIdentifier(scn, qualifier, identifier,
                    updatedIdentifier);
            if (response.statusCode() == SC_OK || response.statusCode() == SC_NO_CONTENT) {
                return getAuthority(scn);
            } else {
                logger.error(String.format("updatedIdentifier - ErrorCode=%s, reasonPhrase=%s", response.statusCode(),
                        response.body()));
                throw new BareException(REMOTE_SERVER_ERRORMESSAGE + response.body());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new BareException(e.getMessage());
        }
    }

    protected Authority getAuthority(String scn)
            throws InterruptedException, BareCommunicationException, BareException {
        try {
            final BareAuthority updatedAuthority = bareConnection.get(scn);
            if (Objects.nonNull(updatedAuthority)) {
                AuthorityConverter authorityConverter = new AuthorityConverter(environment);
                return authorityConverter.asAuthority(updatedAuthority);
            } else {
                logger.error(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
                throw new BareCommunicationException(
                        COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
            }
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new BareException(e.getMessage());
        }
    }

    @Override
    protected Integer getSuccessStatusCode(UpdateAuthorityIdentifierRequest input, Authority output) {
        return SC_OK;
    }
}
