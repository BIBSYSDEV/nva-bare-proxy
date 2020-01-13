package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handler for requests to Lambda function.
 */
public class UpdateAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String AUTHORITY_NOT_FOUND = "Authority not found for 'scn = %s'";
    public static final String TO_MANY_AUTHORITIES_FOUND = "To many authorities found for 'scn = %s'";
    public static final String BODY_ARGS_MISSING = "Nothing to update. 'feideId' and 'orcId' are missing.";
    public static final String MISSING_BODY_ELEMENT_EVENT = "Missing body element 'event'.";
    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String COMMUNICATION_ERROR_WHILE_UPDATING = "Communication failure while updating authority %s";
    public static final String SCN_KEY = "scn";
    public static final String FEIDEID_KEY = "feideId";
    public static final String ORCID_KEY = "orcId";
    public static final String BODY_KEY = "body";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String EMPTY_STRING = "";
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;


    public UpdateAuthorityHandler() {
        this.bareConnection = new BareConnection();
    }

    public UpdateAuthorityHandler(BareConnection bareConnection) {
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
        String orcId = getValueFromJsonObject(bodyEvent, ORCID_KEY);
        return this.updateFeideIdAndOrcId(scn, feideId, orcId);
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

    private GatewayResponse updateFeideIdAndOrcId(String scn, String feideId, String orcId) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            URL bareQueryUrl = bareConnection.generateQueryUrl(scn);
            try (InputStreamReader streamReader = bareConnection.connect(bareQueryUrl)) {
                final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                int numOfAuthoritiesFound = fetchedAuthority.size();
                switch (numOfAuthoritiesFound) {
                    case 1:
                        Authority authority = fetchedAuthority.get(0);
                        if (!StringUtils.isEmpty(feideId)) {
                            authority.setFeideId(feideId);
                        }
                        if (!StringUtils.isEmpty(orcId)) {
                            authority.setOrcId(orcId);
                        }
                        gatewayResponse = updateAuthorityOnBare(scn, authority);
                        break;
                    case 0:
                        gatewayResponse.setErrorBody(String.format(AUTHORITY_NOT_FOUND, scn));
                        gatewayResponse.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
                        break;
                    default:
                        gatewayResponse.setErrorBody(String.format(TO_MANY_AUTHORITIES_FOUND, scn));
                        gatewayResponse.setStatusCode(Response.Status.CONFLICT.getStatusCode());
                        break;
                }
            }
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }

    private GatewayResponse updateAuthorityOnBare(String scn, Authority authority) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try (CloseableHttpResponse response = bareConnection.update(authority)) {
            HttpEntity responseEntity = response.getEntity();
            List<Authority> updatedAuthority;
            try (InputStream contentStream = responseEntity.getContent()) {
                String content = IOUtils.toString(contentStream, StandardCharsets.UTF_8.name());
                System.out.println("bareResponse content: " + content);
                updatedAuthority = authorityConverter.extractAuthoritiesFrom(content);
            }
            if (!updatedAuthority.isEmpty()) {
                gatewayResponse.setBody(new Gson().toJson(updatedAuthority.get(0)));
                gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
            } else {
                gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_UPDATING, scn));
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
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
