package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.nonNull;
import static no.unit.nva.bare.ApplicationConfig.defaultRestObjectMapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.exceptions.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler implements RequestHandler<Map<String, Object>, CustomGatewayResponse> {

    public static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String NAME_KEY = "name";
    public static final String FEIDE_KEY = "feideid";
    public static final String ORCID_KEY = "orcid";
    public static final String ARPID_KEY = "arpId";
    public static final String SCN_KEY = "scn";
    protected static final String MISSING_PARAMETERS = "Missing parameters! Query parameter not set.";
    protected final transient AuthorityConverter authorityConverter;
    protected final transient BareConnection bareConnection;
    private final transient Logger log = LoggerFactory.getLogger(FetchAuthorityHandler.class);

    @JacocoGenerated
    public FetchAuthorityHandler() {
        this(new BareConnection());
    }

    public FetchAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
        this.authorityConverter = new AuthorityConverter();
    }

    /**
     * Main lambda function to get authority metadata from Bare.
     *
     * @param input payload with identifying parameters
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public CustomGatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        CustomGatewayResponse gatewayResponse = new CustomGatewayResponse();

        if (isGetByScnQuery(input)
        ) {
            Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
            String scn = pathParameters.get(SCN_KEY);
            return getAuthorityAndMakeGatewayResponse(gatewayResponse, scn);
        } else {
            if (isSearchQuery(input)) {
                Map<String, String> queryStringParameters =
                    (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
                if (searchQueryContainsArpId(queryStringParameters)) {
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
                    gatewayResponse.setStatusCode(HTTP_BAD_REQUEST);
                    return gatewayResponse;
                }
                try {

                    BareQueryResponse searchResult = bareConnection.searchByAuthorityName(query);

                    final List<Authority> fetchedAuthority = authorityConverter.extractAuthorities(searchResult);
                    log.info(defaultRestObjectMapper.writeValueAsString(fetchedAuthority));

                    gatewayResponse.setBody(defaultRestObjectMapper.writeValueAsString(fetchedAuthority));
                    gatewayResponse.setStatusCode(HTTP_OK);
                } catch (IOException | URISyntaxException | InterruptedException e) {

                    gatewayResponse.setErrorBody(ExceptionUtils.stackTraceInSingleLine(e));
                    gatewayResponse.setStatusCode(HTTP_INTERNAL_ERROR);
                }
            } else {
                gatewayResponse.setErrorBody(MISSING_PARAMETERS);
                gatewayResponse.setStatusCode(HTTP_BAD_REQUEST);
            }
        }

        return gatewayResponse;
    }

    private boolean searchQueryContainsArpId(Map<String, String> queryStringParameters) {
        return nonNull(queryStringParameters) && queryStringParameters.containsKey(ARPID_KEY);
    }

    private boolean isSearchQuery(Map<String, Object> input) {
        return nonNull(input) && input.containsKey(QUERY_STRING_PARAMETERS_KEY);
    }

    private boolean isGetByScnQuery(Map<String, Object> input) {
        return nonNull(input) && input.containsKey(PATH_PARAMETERS_KEY)
               && nonNull(input.get(PATH_PARAMETERS_KEY))
               && nonNull(((Map<String, String>) input.get(PATH_PARAMETERS_KEY)).get(SCN_KEY));
    }

    private CustomGatewayResponse getAuthorityAndMakeGatewayResponse(CustomGatewayResponse gatewayResponse,
                                                                     String arpId) {
        try {
            BareAuthority fetchedAuthority = bareConnection.get(arpId);
            Authority authority = authorityConverter.asAuthority(fetchedAuthority);
            gatewayResponse.setBody(defaultRestObjectMapper.writeValueAsString(authority));
            gatewayResponse.setStatusCode(HTTP_OK);
            return gatewayResponse;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            gatewayResponse.setErrorBody(ExceptionUtils.stackTraceInSingleLine(e));
            gatewayResponse.setStatusCode(HTTP_INTERNAL_ERROR);
            return gatewayResponse;
        }
    }
}
