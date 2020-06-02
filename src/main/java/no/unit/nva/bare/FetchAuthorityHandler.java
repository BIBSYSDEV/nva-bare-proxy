package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    protected static final String MISSING_PARAMETERS = "Missing parameters! Query parameter not set.";
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;
    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String NAME_KEY = "name";
    public static final String FEIDE_KEY = "feideid";
    public static final String ORCID_KEY = "orcid";
    public static final String ARPID_KEY = "arpId";
    private final transient Logger log = Logger.instance();


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
        log.info(input);
        Config.getInstance().checkProperties();
        GatewayResponse gatewayResponse  = new GatewayResponse();
        Gson gson = new Gson().newBuilder().setPrettyPrinting().create();

        if (input != null && input.containsKey(QUERY_STRING_PARAMETERS_KEY)) {
            Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
            if (!Objects.isNull(queryStringParameters) && queryStringParameters.containsKey(ARPID_KEY)) {
                String arpId = queryStringParameters.get(ARPID_KEY);
                try {
                    BareAuthority fetchedAuthority = bareConnection.get(arpId);
                    Authority authority = authorityConverter.asAuthority(fetchedAuthority);
                    gatewayResponse.setBody(gson.toJson(authority));
                    gatewayResponse.setStatusCode(SC_OK);
                    return gatewayResponse;
                } catch (URISyntaxException | IOException | InterruptedException e) {
                    gatewayResponse.setErrorBody(e.getMessage());
                    gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
                    return gatewayResponse;
                }
            }

            String query;
            if (!Objects.isNull(queryStringParameters) && queryStringParameters.containsKey(FEIDE_KEY)) {
                query = queryStringParameters.get(FEIDE_KEY);
            } else if (!Objects.isNull(queryStringParameters) && queryStringParameters.containsKey(ORCID_KEY)) {
                query = queryStringParameters.get(ORCID_KEY);
            } else if (!Objects.isNull(queryStringParameters) && queryStringParameters.containsKey(NAME_KEY)) {
                query = queryStringParameters.get(NAME_KEY);
            } else {
                gatewayResponse.setErrorBody(MISSING_PARAMETERS);
                gatewayResponse.setStatusCode(SC_BAD_REQUEST);
                return gatewayResponse;
            }
            try {
                URL bareUrl = bareConnection.generateQueryUrl(query);
                log.info(bareUrl.toString());
                try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                    final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                    log.info(gson.toJson(fetchedAuthority));
                    gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                    gatewayResponse.setStatusCode(SC_OK);
                }
            } catch (IOException | URISyntaxException e) {
                gatewayResponse.setErrorBody(e.getMessage());
                gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            gatewayResponse.setErrorBody(MISSING_PARAMETERS);
            gatewayResponse.setStatusCode(SC_BAD_REQUEST);
        }

        return gatewayResponse;
    }

}
