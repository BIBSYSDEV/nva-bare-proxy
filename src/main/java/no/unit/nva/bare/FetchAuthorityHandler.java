package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Handler for requests to Lambda function.
 */
public class FetchAuthorityHandler extends AuthorityHandler {


    public FetchAuthorityHandler() {
        super();
    }

    public FetchAuthorityHandler(BareConnection bareConnection) {
        super(bareConnection);
    }

    public GatewayResponse handleRequest(final String input) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Authority inputAuthority = gson.fromJson(input, Authority.class);
        String authorityName = this.selectQueryParameter(inputAuthority);
        try {
            URL bareUrl = bareConnection.generateQueryUrl(authorityName);
            try (InputStreamReader streamReader = bareConnection.connect(bareUrl)) {
                final JsonObject responseObject = (JsonObject) JsonParser.parseReader(streamReader);
                final List<Authority> fetchedAuthority = authorityConverter.getAuthoritiesFrom(responseObject);
                gatewayResponse.setBody(gson.toJson(fetchedAuthority));
                gatewayResponse.setStatus(Response.Status.OK);
            }
        } catch (IOException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return gatewayResponse;
    }

    private String selectQueryParameter(Authority inputAuthority) {
        String queryParam = "";
        if (!inputAuthority.getName().isEmpty()) {
            queryParam = inputAuthority.getName();
        } else if (!inputAuthority.getFeideId().isEmpty()) {
            queryParam = inputAuthority.getFeideId();
        } else if (!inputAuthority.getOrcId().isEmpty()) {
            queryParam = inputAuthority.getOrcId();
        }
        return queryParam;
    }

}
