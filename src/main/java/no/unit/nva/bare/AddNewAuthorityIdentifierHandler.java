package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
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
public class AddNewAuthorityIdentifierHandler extends ApiGatewayHandler<AddNewAuthorityIdentifierRequest, Authority> {

    public static final String MISSING_PATH_PARAMETER_SCN = "Missing from pathParameters: scn";
    public static final String MISSING_PATH_PARAMETER_QUALIFIER = "Missing from pathParameters: qualifier";
    public static final String MISSING_REQUEST_JSON_BODY = "Missing json in body.";
    public static final String MISSING_ATTRIBUTE_IDENTIFIER = "Missing json attribute 'identifier'.";
    public static final String COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY =
            "Communication failure trying to update authority";
    public static final String SCN_KEY = "scn";
    public static final String QUALIFIER_KEY = "qualifier";
    public static final String REMOTE_SERVER_ERRORMESSAGE = "remote server errormessage: ";
    public static final Logger logger = LoggerFactory.getLogger(AddNewAuthorityIdentifierHandler.class);


    public static final List<String> VALID_QUALIFIERS = asList(
            ValidIdentifierKey.FEIDEID.asString(),
            ValidIdentifierKey.ORCID.asString(),
            ValidIdentifierKey.ORGUNITID.asString());

    private transient BareConnection bareConnection;

    /**
     * Default constructor for AddNewAuthorityIdentifierHandler.
     */
    @JacocoGenerated
    public AddNewAuthorityIdentifierHandler() {
        this(new BareConnection());
    }

    /**
     * Constructor for AddNewAuthorityIdentifierHandler.
     *
     * @param bareConnection bareConnection
     */
    public AddNewAuthorityIdentifierHandler(BareConnection bareConnection) {
        super(AddNewAuthorityIdentifierRequest.class, new Environment());
        this.bareConnection = bareConnection;
    }

    @Override
    protected Authority processInput(AddNewAuthorityIdentifierRequest input,
                                     RequestInfo requestInfo,
                                     Context context) throws ApiGatewayException {

        try {
            String scn = requestInfo.getPathParameter(SCN_KEY);
            String inputQualifier = requestInfo.getPathParameter(QUALIFIER_KEY);
            String qualifier = transformQualifier(inputQualifier);

            validateInput(input);
            String identifier = input.getIdentifier();
            AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier(qualifier, identifier);
            return addNewIdentifier(scn, authorityIdentifier);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

    }

    private String transformQualifier(String inputQualifier) {
        if (inputQualifier.equals(ValidIdentifierKey.FEIDEID.asString())) {
            return ValidIdentifierSource.feide.asString();
        }
        return inputQualifier;
    }

    private void validateInput(AddNewAuthorityIdentifierRequest input)
        throws  BadRequestException {
        if (Objects.isNull(input)) {
            throw new BadRequestException(MISSING_REQUEST_JSON_BODY);
        }
        if (StringUtils.isEmpty(input.getIdentifier())) {
            throw new BadRequestException(MISSING_ATTRIBUTE_IDENTIFIER);
        }
    }

    protected Authority addNewIdentifier(String scn, AuthorityIdentifier authorityIdentifier)
            throws ApiGatewayException {
        try {
            HttpResponse<String> response = bareConnection.addNewIdentifier(scn, authorityIdentifier);
            if (responseIsSuccessful(response)) {
                return getAuthority(scn);
            } else {
                logger.error(String.format("addNewIdentifier - ErrorCode=%s, reasonPhrase=%s", response.statusCode(),
                        response.body()));
                throw new BareException(REMOTE_SERVER_ERRORMESSAGE + response.body());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new BareException(e.getMessage());
        }
    }

    private boolean responseIsSuccessful(HttpResponse<String> response) {
        return response.statusCode() == HTTP_OK || response.statusCode() == HTTP_NO_CONTENT;
    }

    private Authority getAuthority(String scn) throws InterruptedException, BareCommunicationException, BareException {
        try {
            final BareAuthority updatedAuthority = bareConnection.get(scn);
            if (Objects.nonNull(updatedAuthority)) {
                AuthorityConverter authorityConverter = new AuthorityConverter();
                return authorityConverter.asAuthority(updatedAuthority);
            } else {
                logger.info(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
                throw new BareCommunicationException(
                        COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY);
            }
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new BareException(e.getMessage());
        }
    }

    @Override
    protected Integer getSuccessStatusCode(AddNewAuthorityIdentifierRequest input, Authority output) {
        return HTTP_OK;
    }

}
