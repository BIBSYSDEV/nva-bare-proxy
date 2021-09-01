package no.unit.nva.bare;

import static java.util.Arrays.asList;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static final Logger logger = LoggerFactory.getLogger(UpdateAuthorityIdentifierHandler.class);

    /**
     * Default constructor for UpdateAuthorityIdentifierHandler.
     */
    @JacocoGenerated
    @JsonCreator
    public UpdateAuthorityIdentifierHandler() {
        this(new BareConnection());
    }

    /**
     * Constructor for UpdateAuthorityIdentifierHandler.
     *
     * @param bareConnection bareConnection
     */
    public UpdateAuthorityIdentifierHandler(BareConnection bareConnection) {
        super(UpdateAuthorityIdentifierRequest.class, new Environment());
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
        throws BadRequestException {
        if (StringUtils.isEmpty(pathParameters.get(SCN_KEY))) {
            throw new BadRequestException(MISSING_PATH_PARAMETER_SCN);
        }
        if (StringUtils.isEmpty(pathParameters.get(QUALIFIER_KEY))) {
            throw new BadRequestException(MISSING_PATH_PARAMETER_QUALIFIER);
        }
        if (!VALID_QUALIFIERS.contains(pathParameters.get(QUALIFIER_KEY))) {
            throw new BadRequestException(INVALID_VALUE_PATH_PARAMETER_QUALIFIER);
        }
        if (Objects.isNull(input)) {
            throw new BadRequestException(MISSING_REQUEST_JSON_BODY);
        }
        if (StringUtils.isEmpty(input.getIdentifier())) {
            throw new BadRequestException(MISSING_ATTRIBUTE_IDENTIFIER);
        }
        if (StringUtils.isEmpty(input.getUpdatedIdentifier())) {
            throw new BadRequestException(MISSING_ATTRIBUTE_UPDATED_IDENTIFIER);
        }
    }

    @JacocoGenerated
    protected Authority updateIdentifier(String scn, String qualifier, String identifier,
                                         String updatedIdentifier) throws BareCommunicationException, BareException {

        try {
            HttpResponse<String> response = bareConnection.updateIdentifier(scn, qualifier, identifier,
                    updatedIdentifier);
            if (response.statusCode() == HttpURLConnection.HTTP_OK
                || response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
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
                AuthorityConverter authorityConverter = new AuthorityConverter();
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
        return HttpURLConnection.HTTP_OK;
    }
}
