package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
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
public class UpdateAuthorityHandler {

    public static final String AUTHORITY_NOT_FOUND = "Authority not found for 'scn = %s'";
    public static final String TO_MANY_AUTHORITIES_FOUND = "To many authorities found for 'scn = %s'";
    public static final String BODY_ARGS_MISSING = "Nothing to update. 'feideId' and 'orcId' are missing.";
    public static final String MISSING_BODY_ELEMENT_EVENT = "Missing body element 'event'.";
    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String COMMUNICATION_FAILURE_WHILE_UPDATING = "Communication failure while updating authority %s";
    public static final String SCN_KEY = "scn";
    public static final String FEIDEID_KEY = "feideId";
    public static final String ORCID_KEY = "orcId";
    public static final String BODY_KEY = "body";
    protected final transient GatewayResponse gatewayResponse = new GatewayResponse();
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;


    public UpdateAuthorityHandler() {
        this.bareConnection = new BareConnection();
    }

    public UpdateAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    public GatewayResponse handleRequest(final APIGatewayProxyRequestEvent input) {
        Map<String, String> pathParameters = input.getPathParameters();
        if (Objects.isNull(pathParameters)) {
            gatewayResponse.setErrorBody(MISSING_PATH_PARAMETER_SCN);
            gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
        } else {
            String scn = pathParameters.get(SCN_KEY);
            String bodyEvent = input.getBody();
            if (StringUtils.isEmpty(scn)) {
                gatewayResponse.setErrorBody(MISSING_PATH_PARAMETER_SCN);
                gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
            } else if (StringUtils.isEmpty(bodyEvent)) {
                gatewayResponse.setErrorBody(MISSING_BODY_ELEMENT_EVENT);
                gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
            } else {
                String feideId = getValueFromJsonObject(bodyEvent, FEIDEID_KEY);
                String orcId = getValueFromJsonObject(bodyEvent, ORCID_KEY);
                if (feideId.isEmpty() && orcId.isEmpty()) {
                    gatewayResponse.setErrorBody(BODY_ARGS_MISSING);
                    gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
                } else {
                    updateIdentifiersOnAuthority(scn, feideId, orcId);
                }
            }
        }
        return gatewayResponse;
    }

    private void updateIdentifiersOnAuthority(String scn, String feideId, String orcId) {
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
                        updateAuthorityOnBare(scn, authority);
                        break;
                    case 0:
                        gatewayResponse.setErrorBody(String.format(AUTHORITY_NOT_FOUND, scn));
                        gatewayResponse.setStatus(Response.Status.NOT_FOUND);
                        break;
                    default:
                        gatewayResponse.setErrorBody(String.format(TO_MANY_AUTHORITIES_FOUND, scn));
                        gatewayResponse.setStatus(Response.Status.CONFLICT);
                        break;
                }
            }
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private void updateAuthorityOnBare(String scn, Authority authority) {
        try (CloseableHttpResponse response = bareConnection.update(authority)) {
            HttpEntity responseEntity = response.getEntity();
            List<Authority> updatedAuthority;
            try (InputStream contentStream = responseEntity.getContent()) {
                String content = IOUtils.toString(contentStream, StandardCharsets.UTF_8.name());
                updatedAuthority = authorityConverter.extractAuthoritiesFrom(content);
            }
            if (!updatedAuthority.isEmpty()) {
                gatewayResponse.setBody(new Gson().toJson(updatedAuthority.get(0)));
                gatewayResponse.setStatus(Response.Status.OK);
            } else {
                gatewayResponse.setErrorBody(String.format(COMMUNICATION_FAILURE_WHILE_UPDATING, scn));
                gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getValueFromJsonObject(String body, String key) {
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(body);
        JsonElement jsonElement = jsonObject.get(BODY_KEY);
        if (Objects.nonNull(jsonElement)) {
            jsonElement = ((JsonObject) jsonElement).get(key);
        }
        return Objects.isNull(jsonElement) ? "" : jsonElement.getAsString();
    }

}
