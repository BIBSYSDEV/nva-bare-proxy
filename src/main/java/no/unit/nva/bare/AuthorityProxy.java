package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class AuthorityProxy implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final transient String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final transient String ERROR_KEY = "error";
    public static final String EMPTY_STRING = "";
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
    public GatewayResponse handleRequest(final Map<String, Object> input, final Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String authoritySource = (String) input.get("body");
        Authority inputAuthority = gson.fromJson(authoritySource, Authority.class);
        GatewayResponse gatewayResponse = new GatewayResponse();
        String authorityName = this.selectQueryParameter(inputAuthority);
        try {
            URL bareUrl = bareConnection.generateQueryUrl(authorityName);
            // The eventually thrown exceptions here catches in the outer try/catch as they are IOExceptions as well
            // and the inputStream is closed by this try automatically
            try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
            }
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setBody(this.getErrorAsJson(e.getMessage()));
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }

    private String selectQueryParameter(Authority inputAuthority) {
        String queryParam = EMPTY_STRING;
        String name = Optional.ofNullable(inputAuthority.getName()).orElse(EMPTY_STRING);
        String feideId = Optional.ofNullable(inputAuthority.getFeideId()).orElse(EMPTY_STRING);
        String orcId = Optional.ofNullable(inputAuthority.getOrcId()).orElse(EMPTY_STRING);
        if (StringUtils.isNotEmpty(name)) {
            queryParam = name;
        } else if (StringUtils.isNotEmpty(feideId)) {
            queryParam = feideId;
        } else if (StringUtils.isNotEmpty(orcId)) {
            queryParam = orcId;
        }
        return queryParam;
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
