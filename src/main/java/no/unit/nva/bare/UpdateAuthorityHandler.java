package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class UpdateAuthorityHandler extends AuthorityHandler {

    public static final String AUTHORITY_NOT_FOUND = "Authority not found for 'scn = %s'";
    public static final String TO_MANY_AUTHORITIES_FOUND = "To many authorities found for 'scn = %s'";
    public static final String BODY_ARGS_MISSING = "Nothing to update. 'feideId' and 'orcId' are missing.";
    public static final String MISSING_BODY_ELEMENT_EVENT = "Missing body element 'event'.";
    public static final String MISSING_PATH_PARAMETER_SCN = "Missing path parameter 'scn'.";
    public static final String SCN_KEY = "scn";

    public UpdateAuthorityHandler() {
        super();
    }

    public UpdateAuthorityHandler(BareConnection bareConnection) {
        super(bareConnection);
    }

    public GatewayResponse handleRequest(final APIGatewayProxyRequestEvent input) {
        Map<String, String> pathParameters = input.getPathParameters();
        String scn = pathParameters.get(SCN_KEY);
        String body = input.getBody();
        if (StringUtils.isEmpty(scn)) {
            gatewayResponse.setErrorBody(MISSING_PATH_PARAMETER_SCN);
            gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
        } else if (StringUtils.isEmpty(body)) {
            gatewayResponse.setErrorBody(MISSING_BODY_ELEMENT_EVENT);
            gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
        } else {
            JsonObject jsonObject = (JsonObject) JsonParser.parseString(body);
            String feideId = jsonObject.get(AuthorityConverter.FEIDE_KEY).getAsString();
            String orcId = jsonObject.get(AuthorityConverter.ORCID_KEY).getAsString();
            if (StringUtils.isEmpty(feideId) && orcId.isEmpty()) {
                gatewayResponse.setErrorBody(BODY_ARGS_MISSING);
                gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
            } else {
                try {
                    URL bareUrl = bareConnection.generateQueryUrl(scn);
                    try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                        final JsonObject responseObject = (JsonObject) JsonParser.parseReader(streamReader);
                        final List<Authority> fetchedAuthority = authorityConverter.getAuthoritiesFrom(responseObject);
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
                                // do the update
                                gatewayResponse.setBody(new Gson().toJson(authority));
                                gatewayResponse.setStatus(Response.Status.OK);
                                break;
                            case 0:
                                gatewayResponse.setErrorBody(String.format(AUTHORITY_NOT_FOUND, scn));
                                gatewayResponse.setStatus(Response.Status.NOT_FOUND);
                                break;
                            default:
                                gatewayResponse.setErrorBody(String.format(TO_MANY_AUTHORITIES_FOUND, scn));
                                gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
                            }
                        }
                } catch (Exception e) {
                    gatewayResponse.setErrorBody(e.getMessage());
                    gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return gatewayResponse;
    }

}
