package no.unit.nva.bare;

import static java.util.Objects.nonNull;
import static nva.commons.core.JsonUtils.objectMapper;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler implements RequestHandler<Map<String, Object>, CustomGatewayResponse> {

    protected static final String MISSING_PARAMETERS = "Missing parameters! Query parameter not set.";
    protected final transient AuthorityConverter authorityConverter;
    protected final transient BareConnection bareConnection;
    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";


    public static final String NAME_KEY = "name";
    public static final String FEIDE_KEY = "feideid";
    public static final String ORCID_KEY = "orcid";
    public static final String ARPID_KEY = "arpId";
    public static final String SCN_KEY = "scn";

    private final transient Logger log = LoggerFactory.getLogger(FetchAuthorityHandler.class);



    @JacocoGenerated
    public FetchAuthorityHandler() {
        this(new BareConnection(), new Environment());
    }

    public FetchAuthorityHandler(BareConnection bareConnection, Environment environment) {
        this.bareConnection = bareConnection;
        this.authorityConverter = new AuthorityConverter(environment);
    }

    /**
     * Main lambda function to get authority metadata from Bare.
     * @param input payload with identifying parameters
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public CustomGatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        Config.getInstance().checkProperties();
        CustomGatewayResponse gatewayResponse  = new CustomGatewayResponse();


        if (nonNull(input) && input.containsKey(PATH_PARAMETERS_KEY)
                && nonNull(input.get(PATH_PARAMETERS_KEY))
                && nonNull(((Map<String, String>) input.get(PATH_PARAMETERS_KEY)).get(SCN_KEY))
        ) {
            Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
            String scn = pathParameters.get(SCN_KEY);
            return getAuthorityAndMakeGatewayResponse(gatewayResponse, scn);

        } else {
            if (nonNull(input) && input.containsKey(QUERY_STRING_PARAMETERS_KEY)) {
                Map<String, String> queryStringParameters =
                        (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
                if (nonNull(queryStringParameters) && queryStringParameters.containsKey(ARPID_KEY)) {
                    String arpId = queryStringParameters.get(ARPID_KEY);
                    return getAuthorityAndMakeGatewayResponse(gatewayResponse, arpId);
                }

                String query;
                if (nonNull(queryStringParameters) && queryStringParameters.containsKey(FEIDE_KEY)) {
                    query = queryStringParameters.get(FEIDE_KEY);
                } else if (nonNull(queryStringParameters) && queryStringParameters.containsKey(ORCID_KEY)) {
                    query = queryStringParameters.get(ORCID_KEY);
                } else if (nonNull(queryStringParameters) && queryStringParameters.containsKey(NAME_KEY)) {
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
                        final List<Authority> fetchedAuthority =
                                authorityConverter.extractAuthoritiesFrom(streamReader);
                        log.info(objectMapper.writeValueAsString(fetchedAuthority));
                        gatewayResponse.setBody(objectMapper.writeValueAsString(fetchedAuthority));
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
        }

        return gatewayResponse;
    }

    private CustomGatewayResponse getAuthorityAndMakeGatewayResponse(CustomGatewayResponse gatewayResponse,
                                                                     String arpId) {
        try {
            BareAuthority fetchedAuthority = bareConnection.get(arpId);
            Authority authority = authorityConverter.asAuthority(fetchedAuthority);
            gatewayResponse.setBody(objectMapper.writeValueAsString(authority));
            gatewayResponse.setStatusCode(SC_OK);
            return gatewayResponse;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            return gatewayResponse;
        }
    }

}
