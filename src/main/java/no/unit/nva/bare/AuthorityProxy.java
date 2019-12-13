package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class AuthorityProxy implements RequestHandler<String, Object> {

    private final BareConnection bareConnection;

    public AuthorityProxy() {
        bareConnection = new BareConnection();
    }

    public AuthorityProxy(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    @Override
    public Object handleRequest(final String input, final Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Authority inputAuthority = gson.fromJson(input, Authority.class);
        GatewayResponse gatewayResponse = new GatewayResponse("{}", headers, Response.Status.INTERNAL_SERVER_ERROR);
        try {
            String authorityName = inputAuthority.getName();
            URL bareUrl = bareConnection.setUpQueryUrl(authorityName);
            final InputStreamReader streamReader = bareConnection.connect(bareUrl);
            final List<Authority> fetchedAuthority = this.getAuthorities(streamReader);
            gatewayResponse.setBody(gson.toJson(fetchedAuthority));
            gatewayResponse.setStatus(Response.Status.OK);
        } catch (IOException e) {
            gatewayResponse.setBody("{\"error\": \"" + e.getMessage() + "\"}");
            gatewayResponse.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return gatewayResponse;
    }

    private List<Authority> getAuthorities(InputStreamReader streamReader) {
        ArrayList<Authority> authorityList;
        final JsonObject responseObject = (JsonObject) JsonParser.parseReader(streamReader);
        int numFound = responseObject.get("numFound").getAsInt();
        authorityList = new ArrayList<>(numFound);
        final JsonArray results = responseObject.get("results").getAsJsonArray();
        for (JsonElement jsonElement : results) {
            final JsonObject result = (JsonObject) jsonElement;
            Authority authority = new Authority();
            final JsonArray marcdata = result.get("marcdata").getAsJsonArray();
            for (JsonElement marcdatum : marcdata) {
                final JsonObject marc = (JsonObject) marcdatum;
                if ("100".equals(marc.get("tag").getAsString())) {
                    JsonArray subfields = marc.get("subfields").getAsJsonArray();
                    for (JsonElement subfield : subfields) {
                        JsonObject sub = (JsonObject) subfield;
                        if ("a".equals(sub.get("subcode").getAsString())) {
                            authority.setName(sub.get("value").getAsString());
                        }
                        if ("d".equals(sub.get("subcode").getAsString())) {
                            authority.setBirthDate(sub.get("value").getAsString());
                        }
                    }
                }
                final JsonObject identifiersMap = (JsonObject) result.get("identifiersMap");
                authority.setScn(this.getValueFromJsonArray(identifiersMap, "scn"));
                authority.setFeideId(this.getValueFromJsonArray(identifiersMap, "feide"));
                authority.setOrcId(this.getValueFromJsonArray(identifiersMap, "orcid"));
            }
            authorityList.add(authority);
        }
        return authorityList;
    }

    protected String getValueFromJsonArray(JsonObject jsonObject, String key) throws ArrayIndexOutOfBoundsException {
        String value = "";
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement != null) {
            value = jsonElement.getAsJsonArray().get(0).getAsString();
        }
        return value;
    }

}
