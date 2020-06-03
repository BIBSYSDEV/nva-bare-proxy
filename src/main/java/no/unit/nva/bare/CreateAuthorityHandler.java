package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Handler for requests to Lambda function creating an authority in ARP.
 */
public class CreateAuthorityHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String BODY_KEY = "body";
    public static final String NAME_KEY = "invertedname";
    public static final String EMPTY_STRING = "";
    public static final String MISSING_EVENT_ELEMENT_BODY = "Missing event element 'body'.";
    public static final String BODY_ARGS_MISSING = "Nothing to create. 'name' is missing.";
    public static final String COMMUNICATION_ERROR_WHILE_CREATING = "Communication failure while creating new "
            + "authority with name='%s'";
    public static final String COMMA = ",";
    public static final String MALFORMED_NAME_VALUE = "The name value seems not to be in inverted form.";
    protected final transient BareConnection bareConnection;
    private final transient Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private final transient Logger log = Logger.instance();

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
            log.error(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(SC_BAD_REQUEST);
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
        try {
            HttpResponse<String> response = bareConnection.createAuthority(bareAuthority);
            log.info("response (from bareConnection)=" + response);
            if (response.statusCode() == SC_CREATED
                    || response.statusCode() == SC_OK) { //201
                BareAuthority createdAuthority = gson.fromJson(response.body(), BareAuthority.class);
                if (Objects.nonNull(createdAuthority)) {
                    final AuthorityResponse authority = authorityConverter.asAuthority(createdAuthority);
                    gatewayResponse.setBody(gson.toJson(authority, AuthorityResponse.class));
                    gatewayResponse.setStatusCode(SC_OK);
                } else {
                    log.error(String.format(COMMUNICATION_ERROR_WHILE_CREATING, name));
                    gatewayResponse.setErrorBody(String.format(COMMUNICATION_ERROR_WHILE_CREATING, name));
                    gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                log.error("Error: " + response.body());
                log.error("new authority looked like this: \n" + gson.toJson(bareAuthority));
                gatewayResponse.setErrorBody(response.statusCode() + ": " + response.body());
                gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.error(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(SC_INTERNAL_SERVER_ERROR);
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
        final String nameValue = getValueFromJsonObject(eventBody, NAME_KEY);
        if (StringUtils.isEmpty(nameValue)) {
            throw new RuntimeException(BODY_ARGS_MISSING);
        } else if (!nameValue.contains(COMMA)) {
            throw new RuntimeException(MALFORMED_NAME_VALUE);
        }
    }
}
