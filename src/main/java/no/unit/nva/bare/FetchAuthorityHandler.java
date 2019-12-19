package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler {

    public static final String EMPTY_STRING = "";
    protected static final String MISSING_BODY_ELEMENTS = "Missing body paramters %s";
    protected final GatewayResponse gatewayResponse = new GatewayResponse();
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();
    protected final transient BareConnection bareConnection;


    public FetchAuthorityHandler() {
        this.bareConnection = new BareConnection();
    }

    public FetchAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    public GatewayResponse handleRequest(final String input) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Authority inputAuthority = gson.fromJson(input, Authority.class);
        if (Objects.nonNull(inputAuthority)) {
            String authorityName = this.selectQueryParameter(inputAuthority);
            try {
                URL bareUrl = bareConnection.generateQueryUrl(authorityName);
                try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                    final List<Authority> fetchedAuthority = authorityConverter.extractAuthoritiesFrom(streamReader);
                    gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                    gatewayResponse.setStatus(Response.Status.OK);
                }
            } catch (IOException | URISyntaxException e) {
                gatewayResponse.setErrorBody(e.getMessage());
                gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
            }
        } else {
            gatewayResponse.setErrorBody(String.format(MISSING_BODY_ELEMENTS, input));
            gatewayResponse.setStatus(Response.Status.BAD_REQUEST);
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
