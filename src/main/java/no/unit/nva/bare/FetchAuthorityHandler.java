package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String EMPTY_STRING = "";
    protected static final String MISSING_BODY = "Missing body";
    public static final String BODY_KEY = "body";
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;


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
    public GatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        System.out.println(input);
        Config.getInstance().checkProperties();
        GatewayResponse gatewayResponse  = new GatewayResponse();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String authoritySource = (String) input.get(BODY_KEY);
        Authority inputAuthority = gson.fromJson(authoritySource, Authority.class);
        if (Objects.nonNull(inputAuthority)) {
            String authorityName = this.selectQueryParameter(inputAuthority);
            try {
                URL bareUrl = bareConnection.generateQueryUrl(authorityName);
                try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                    final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                    System.out.println(gson.toJson(fetchedAuthority));
                    gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                    gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                }
            } catch (IOException | URISyntaxException e) {
                gatewayResponse.setErrorBody(e.getMessage());
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        } else {
            gatewayResponse.setErrorBody(MISSING_BODY);
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }
        return gatewayResponse;
    }

    protected String selectQueryParameter(Authority inputAuthority) {
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

}
