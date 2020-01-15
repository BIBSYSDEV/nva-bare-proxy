package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    protected static final String MISSING_PARAMETERS = "Missing parameters! Neither 'feideId' nor 'name' is set";
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;
    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String NAME_KEY = "name";
    public static final String FEIDE_KEY = "feideId";
    public static final String ORCID_KEY = "orcId";


    public FetchAuthorityHandler() {
        this.bareConnection = new BareConnection();
    }

    public FetchAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    /**
     * Main lambda function to get authority metadata from Bare.
     * @param input payload with identifying parameters
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        System.out.println(input);
        Config.getInstance().checkProperties();
        GatewayResponse gatewayResponse  = new GatewayResponse();

        if (input != null && input.containsKey(QUERY_STRING_PARAMETERS_KEY)) {
            String query;
            Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
            if (queryStringParameters.containsKey(FEIDE_KEY)) {
                query = queryStringParameters.get(FEIDE_KEY);
            } else if (queryStringParameters.containsKey(ORCID_KEY)) {
                query = queryStringParameters.get(ORCID_KEY);
            } else if (queryStringParameters.containsKey(NAME_KEY)) {
                query = queryStringParameters.get(NAME_KEY);
            } else {
                gatewayResponse.setErrorBody(MISSING_PARAMETERS);
                gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
                return gatewayResponse;
            }
            try {
                URL bareUrl = bareConnection.generateQueryUrl(query);
                System.out.println(bareUrl.toString());
                try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                    final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                    Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                    System.out.println(gson.toJson(fetchedAuthority));
                    gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                    gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                }
            } catch (IOException | URISyntaxException e) {
                gatewayResponse.setErrorBody(e.getMessage());
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        } else {
            gatewayResponse.setErrorBody(MISSING_PARAMETERS);
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        return gatewayResponse;
    }

}
