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
    public static final String MISSING_BODY_ELEMENT_EVENT = "Missing body element 'event'.";
    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String COMMUNICATION_ERROR_WHILE_UPDATING = "Communication failure while updating authority %s";
    private static final String NOTHING_TO_DO = "Nothing to do. Identifier exists.";
    public static final String SCN_KEY = "scn";
    public static final String FEIDEID_KEY = "feideid";
    public static final String ORCID_KEY = "orcid";
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
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }
        String bodyEvent = (String) input.get(BODY_KEY);
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        String scn = pathParameters.get(SCN_KEY);
        String feideId = getValueFromJsonObject(bodyEvent, FEIDEID_KEY);
        if (StringUtils.isNotEmpty(feideId)) {
            gatewayResponse = addIdentifier(scn, new AuthorityIdentifier(BareConnection.FEIDE, feideId));
        }
        String orcId = getValueFromJsonObject(bodyEvent, ORCID_KEY);
        if (StringUtils.isNotEmpty(orcId)) {
            gatewayResponse = addIdentifier(scn, new AuthorityIdentifier(BareConnection.ORCID, orcId));
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
        String bodyEvent = (String) input.get(BODY_KEY);
        if (StringUtils.isEmpty(bodyEvent)) {
            throw new RuntimeException(MISSING_BODY_ELEMENT_EVENT);
        }
        if (StringUtils.isEmpty(getValueFromJsonObject(bodyEvent, FEIDEID_KEY))
                && StringUtils.isEmpty(getValueFromJsonObject(bodyEvent, ORCID_KEY))) {
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
                    gatewayResponse.setErrorBody(NOTHING_TO_DO);
                    gatewayResponse.setStatusCode(Response.Status.NO_CONTENT.getStatusCode());
                }
            } else {
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

    protected GatewayResponse updateAuthorityOnBare(String scn, AuthorityIdentifier authority) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try (CloseableHttpResponse response = bareConnection.addIdentifier(scn, authority)) {
            int responseCode = response.getStatusLine().getStatusCode();
            // Somewhat strange code (204) returned from bare when OK
            if (responseCode == Response.Status.NO_CONTENT.getStatusCode()) {
                try {
                    final BareAuthority updatedAuthority = bareConnection.get(scn);

                    if (Objects.nonNull(updatedAuthority)) {
                        gatewayResponse.setBody(new Gson().toJson(updatedAuthority));
                        gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                    } else {
                        gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_UPDATING, scn));
                        gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                } catch (IOException | URISyntaxException e) {
                    gatewayResponse.setErrorBody(e.getMessage());
                    gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                }
            } else {
                gatewayResponse.setErrorBody(response.getStatusLine().getReasonPhrase());
                gatewayResponse.setStatusCode(responseCode);
            }
        } catch (IOException | URISyntaxException e) {
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
