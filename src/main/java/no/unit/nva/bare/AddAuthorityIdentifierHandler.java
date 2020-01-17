package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * Handler for requests to Lambda function.
 */
public class AddAuthorityIdentifierHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String AUTHORITY_NOT_FOUND = "Authority not found for 'scn = %s'";
    public static final String BODY_ARGS_MISSING = "Nothing to update. 'feideid' and 'orcid' are missing.";
    public static final String MISSING_EVENT_ELEMENT_BODY = "Missing event element 'body'.";
    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String COMMUNICATION_ERROR_WHILE_UPDATING = "Communication failure while updating authority %s";
    private static final String NOTHING_TO_DO = "Nothing to do. Identifier exists.";
    public static final String SCN_KEY = "scn";
    public static final String FEIDEID_KEY = ValidIdentifierKey.FEIDEID.asString();
    public static final String ORCID_KEY = ValidIdentifierKey.ORCID.asString();
    public static final String ORGUNITID_KEY = ValidIdentifierKey.ORGUNITID.asString();
    public static final String BODY_KEY = "body";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String EMPTY_STRING = "";
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
        String bodyEvent = (String) input.get(BODY_KEY);
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        String scn = pathParameters.get(SCN_KEY);
        String feideId = getValueFromJsonObject(bodyEvent, FEIDEID_KEY);
        if (StringUtils.isNotEmpty(feideId)) {
            gatewayResponse =
                    addIdentifier(scn, new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), feideId));
        }
        String orcId = getValueFromJsonObject(bodyEvent, ORCID_KEY);
        if (StringUtils.isNotEmpty(orcId)) {
            gatewayResponse =
                    addIdentifier(scn, new AuthorityIdentifier(ValidIdentifierSource.orcid.asString(), orcId));
        }
        String orgUnitId = getValueFromJsonObject(bodyEvent, ORGUNITID_KEY);
        if (StringUtils.isNotEmpty(orgUnitId)) {
            gatewayResponse =
                    addIdentifier(scn, new AuthorityIdentifier(ValidIdentifierSource.orgunitid.asString(), orgUnitId));
        }
        return gatewayResponse;
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
        String eventBody = (String) input.get(BODY_KEY);
        if (StringUtils.isEmpty(eventBody)) {
            throw new RuntimeException(MISSING_EVENT_ELEMENT_BODY);
        }
        if (StringUtils.isEmpty(getValueFromJsonObject(eventBody, ValidIdentifierKey.FEIDEID.asString()))
                && StringUtils.isEmpty(getValueFromJsonObject(eventBody, ValidIdentifierKey.ORCID.asString()))
                && StringUtils.isEmpty(getValueFromJsonObject(eventBody, ValidIdentifierKey.ORGUNITID.asString()))) {
            throw new RuntimeException(BODY_ARGS_MISSING);
        }
    }

    protected GatewayResponse addIdentifier(String scn, AuthorityIdentifier authorityIdentifier) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            BareAuthority fetchedAuthority = bareConnection.get(scn);
            if (Objects.nonNull(fetchedAuthority)) {
                System.out.println("fetchedAuthority=" + fetchedAuthority);
                if (!fetchedAuthority.hasIdentifier(authorityIdentifier)) {
                    gatewayResponse = updateAuthorityOnBare(scn, authorityIdentifier);
                } else {
                    System.out.println(NOTHING_TO_DO);
                    gatewayResponse.setErrorBody(NOTHING_TO_DO);
                    gatewayResponse.setStatusCode(Response.Status.NO_CONTENT.getStatusCode());
                }
            } else {
                System.out.println(String.format(AUTHORITY_NOT_FOUND, scn));
                gatewayResponse.setErrorBody(String.format(AUTHORITY_NOT_FOUND, scn));
                gatewayResponse.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }

    protected GatewayResponse updateAuthorityOnBare(String scn, AuthorityIdentifier authorityIdentifier) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try (CloseableHttpResponse response = bareConnection.addIdentifier(scn, authorityIdentifier)) {
            int responseCode = response.getStatusLine().getStatusCode();
            // Somewhat strange code (204) returned from bare when OK
            if (responseCode == Response.Status.NO_CONTENT.getStatusCode()) {
                try {
                    final BareAuthority updatedAuthority = bareConnection.get(scn);

                    if (Objects.nonNull(updatedAuthority)) {
                        AuthorityConverter authorityConverter = new AuthorityConverter();
                        final Authority authority = authorityConverter.asAuthority(updatedAuthority);
                        gatewayResponse.setBody(new Gson().toJson(authority));
                        gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                    } else {
                        System.out.println(String.format(COMMUNICATION_ERROR_WHILE_UPDATING, scn));
                        gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_UPDATING, scn));
                        gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                } catch (IOException | URISyntaxException e) {
                    System.out.println(e);
                    gatewayResponse.setErrorBody(e.getMessage());
                    gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                }
            } else {
                System.out.println("Error... " + response.getStatusLine().getReasonPhrase());
                gatewayResponse.setErrorBody(response.getStatusLine().getReasonPhrase());
                gatewayResponse.setStatusCode(responseCode);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }

    protected String getValueFromJsonObject(String body, String key) {
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(body);
        JsonElement jsonElement = jsonObject.get(key);
        return Objects.isNull(jsonElement) ? EMPTY_STRING : jsonElement.getAsString();
    }

}
