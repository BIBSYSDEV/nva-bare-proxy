package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Objects;

/**
 * Handler for requests to Lambda function creating an authority in ARP.
 */
public class CreateAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String BODY_KEY = "body";
    public static final String NAME_KEY = "name";
    public static final String EMPTY_STRING = "";
    public static final String MISSING_EVENT_ELEMENT_BODY = "Missing event element 'body'.";
    public static final String BODY_ARGS_MISSING = "Nothing to create. 'name' is missing.";
    public static final String COMMUNICATION_ERROR_WHILE_CREATING = "Communication failure while creating new "
            + "authority with name='%s'";
    protected final transient BareConnection bareConnection;

    public CreateAuthorityHandler(BareConnection bareConnection) {
        this.bareConnection = bareConnection;
    }

    public CreateAuthorityHandler() {
        this.bareConnection = new BareConnection();
    }

    @Override
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }
        String bodyEvent = (String) input.get(BODY_KEY);
        String nameValue = getValueFromJsonObject(bodyEvent, NAME_KEY);
        //todo: check if name is in inverted form
        gatewayResponse = createAuthorityOnBare(nameValue);
        return gatewayResponse;
    }

    protected GatewayResponse createAuthorityOnBare(String name) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        AuthorityConverter authorityConverter = new AuthorityConverter();
        BareAuthority bareAuthority = authorityConverter.buildAuthority(name);
        try (CloseableHttpResponse response = bareConnection.createAuthority(bareAuthority)) {
            System.out.println("response (from bareConnection)=" + response);
            if (response.getStatusLine().getStatusCode() == Response.Status.CREATED.getStatusCode()) { //201
                try (Reader streamReader = new InputStreamReader(response.getEntity().getContent())) {
                    BareAuthority createdAuthority = new Gson().fromJson(streamReader, BareAuthority.class);
                    if (Objects.nonNull(createdAuthority)) {
                        final Authority authority = authorityConverter.asAuthority(createdAuthority);
                        Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
                        gatewayResponse.setBody(gson.toJson(authority));
                        gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
                    } else {
                        System.out.println(String.format(COMMUNICATION_ERROR_WHILE_CREATING, name));
                        gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_CREATING, name));
                        gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                }
            } else {
                System.out.println("Error..? " + response.getStatusLine().getReasonPhrase());
                gatewayResponse.setErrorBody(response.getStatusLine().getReasonPhrase());
                gatewayResponse.setStatusCode(response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return gatewayResponse;
    }


    protected String getValueFromJsonObject(String body, String key) {
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(body);
        JsonElement jsonElement = jsonObject.get(key);
        return Objects.isNull(jsonElement) ? EMPTY_STRING : jsonElement.getAsString();
    }

    private void checkParameters(Map<String, Object> input) {
        String eventBody = (String) input.get(BODY_KEY);
        if (StringUtils.isEmpty(eventBody)) {
            throw new RuntimeException(MISSING_EVENT_ELEMENT_BODY);
        }
        if (StringUtils.isEmpty(getValueFromJsonObject(eventBody, NAME_KEY))) {
            throw new RuntimeException(BODY_ARGS_MISSING);
        }
    }
}
