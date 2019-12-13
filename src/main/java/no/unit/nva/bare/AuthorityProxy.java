package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class AuthorityProxy implements RequestHandler<String, Object> {

    public static final transient String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final transient String ERROR_KEY = "error";
    private final transient BareConnection bareConnection;
    private final transient AuthorityConverter authorityConverter;


    public AuthorityProxy() {
        bareConnection = new BareConnection();
        authorityConverter = new AuthorityConverter();
    }

    public AuthorityProxy(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
        authorityConverter = new AuthorityConverter();
    }

    @Override
    public Object handleRequest(final String input, final Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Authority inputAuthority = gson.fromJson(input, Authority.class);
        GatewayResponse gatewayResponse = new GatewayResponse("{}", headers, Response.Status.INTERNAL_SERVER_ERROR);
        String authorityName = inputAuthority.getName();
        try {
            URL bareUrl = bareConnection.generateQueryUrl(authorityName);
            try (InputStreamReader streamReader = bareConnection.connect(bareUrl)){
                final JsonObject responseObject = (JsonObject) JsonParser.parseReader(streamReader);
                final List<Authority> fetchedAuthority = authorityConverter.getAuthoritiesFrom(responseObject);
                gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                gatewayResponse.setStatus(Response.Status.OK);
            }
        } catch (IOException e) {
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
