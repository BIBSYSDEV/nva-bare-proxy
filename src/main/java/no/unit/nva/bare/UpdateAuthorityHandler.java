package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class UpdateAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final transient String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final transient String ERROR_KEY = "error";
    private final transient BareConnection bareConnection;
    private final transient AuthorityConverter authorityConverter;


    public UpdateAuthorityHandler() {
        bareConnection = new BareConnection();
        authorityConverter = new AuthorityConverter();
    }

    public UpdateAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
        authorityConverter = new AuthorityConverter();
    }

    @Override
    public GatewayResponse handleRequest(final Map<String, Object> input, final Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String scn = pathParameters.get("scn");
        GatewayResponse gatewayResponse = new GatewayResponse("{}", headers, Response.Status.INTERNAL_SERVER_ERROR);
        try {
            URL bareUrl = bareConnection.generateQueryUrl(scn);
            try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                final JsonObject responseObject = (JsonObject) JsonParser.parseReader(streamReader);
                final List<Authority> fetchedAuthority = authorityConverter.getAuthoritiesFrom(responseObject);
                if (fetchedAuthority.size() == 1) {
                    Authority authority = fetchedAuthority.get(0);
//                    authority.setFeideId(feideId);
//                    authority.setOrcId(orcId);
                } else {
                    // to many or to less authorities -> we cannot do it.
                }
//                gatewayResponse.setBody(gson.toJson(fetchedAuthority));
//                gatewayResponse.setStatus(Response.Status.OK);
            }
        } catch (Exception e) {
            gatewayResponse.setBody(this.getErrorAsJson(e.getMessage()));
            gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return gatewayResponse;
    }

    /**
     * Get error message as a json string.
     *
     * @param message message from exception
     * @return String containing an error message as json
     */
    protected String getErrorAsJson(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        return json.toString();
    }


}
