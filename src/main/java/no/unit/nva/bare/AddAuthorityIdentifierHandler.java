package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handler for requests to Lambda function.
 */
public class AddAuthorityIdentifierHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String MISSING_PATH_PARAMETER_QUALIFIER = "Missing path parameter 'qualifier'.";
    public static final String INVALID_VALUE_PATH_PARAMETER_QUALIFIER = "Invalid path parameter 'qualifier'.";
    public static final String MISSING_PATH_PARAMETER_IDENTIFIER = "Missing path parameter 'identifier'.";
    public static final String COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY = "Communication failure while updating authority %s";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String SCN_KEY = "scn";
    public static final String QUALIFIER_KEY = "qualifier";
    public static final String IDENTIFIER_KEY = "identifier";
    public static final int ERROR_CALLING_REMOTE_SERVER = Response.Status.BAD_GATEWAY.getStatusCode();
    public static final String REMOTE_SERVER_ERRORMESSAGE = "remote server errormessage: ";

    public static final List<String> VALID_QUALIFIERS = asList(ValidIdentifierKey.FEIDEID.asString(),
            ValidIdentifierKey.ORCID.asString(), ValidIdentifierKey.ORGUNITID.asString());

    protected final transient BareConnection bareConnection;


    public AddAuthorityIdentifierHandler() {
        this.bareConnection = new BareConnection();
    }

    public AddAuthorityIdentifierHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    /**
     * Main lambda function to update feideId or/and orcId on Bare authority metadata.
     *
     * @param input payload with body-parameter containing the authority metadata
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        String scn = pathParameters.get(SCN_KEY);
        String qualifier = pathParameters.get(QUALIFIER_KEY);
        String identifier = pathParameters.get(IDENTIFIER_KEY);

        return addIdentifier(scn, qualifier, identifier);
    }

    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        if (Objects.isNull(pathParameters)) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_SCN);
        }
        if (StringUtils.isEmpty(pathParameters.get(SCN_KEY))) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_SCN);
        }
        if (StringUtils.isEmpty(pathParameters.get(QUALIFIER_KEY))) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_QUALIFIER);
        }
        if (!VALID_QUALIFIERS.contains(pathParameters.get(QUALIFIER_KEY))) {
            throw new RuntimeException(INVALID_VALUE_PATH_PARAMETER_QUALIFIER);
        }
        if (StringUtils.isEmpty(pathParameters.get(IDENTIFIER_KEY))) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_IDENTIFIER);
        }
    }

    protected GatewayResponse addIdentifier(String scn, String qualifier, String identifier) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try (CloseableHttpResponse response = bareConnection.addIdentifier(scn, qualifier, identifier)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("response (from bareConnection)=" + response);
            if (responseCode == Response.Status.OK.getStatusCode()) {
                try {
                    final BareAuthority updatedAuthority = bareConnection.get(scn);
                    if (Objects.nonNull(updatedAuthority)) {
                        AuthorityConverter authorityConverter = new AuthorityConverter();
                        final Authority authority = authorityConverter.asAuthority(updatedAuthority);
                        gatewayResponse.setBody(new Gson().toJson(authority));
                        gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                    } else {
                        System.out.println(String.format(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY, scn));
                        gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY, scn));
                        gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                } catch (IOException | URISyntaxException e) {
                    System.out.println(e);
                    gatewayResponse.setErrorBody(e.getMessage());
                    gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                }
            } else {
                System.out.println("addIdentifier - ErrorCode=" + response.getStatusLine().getStatusCode()
                        + ",  reasonPhrase=" + response.getStatusLine().getReasonPhrase());
                gatewayResponse.setErrorBody(REMOTE_SERVER_ERRORMESSAGE + response.getStatusLine().getReasonPhrase());
                gatewayResponse.setStatusCode(ERROR_CALLING_REMOTE_SERVER);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }

}
