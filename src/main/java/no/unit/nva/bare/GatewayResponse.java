package no.unit.nva.bare;

import com.google.gson.JsonObject;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    public static final String EMPTY_JSON = "{}";
    private String body;
    private final Map<String, String> headers;
    private Response.Status status;
    private static final transient String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final transient String ERROR_KEY = "error";

    public GatewayResponse() {
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.body = EMPTY_JSON;
        this.headers = this.generateDefaultHeaders();
    }

    public GatewayResponse(final String body, final Response.Status status) {
        this.status = status;
        this.body = body;
        this.headers = this.generateDefaultHeaders();
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    /**
     * Set error message as a json string to body.
     *
     * @param message message from exception
     */
    public void setErrorBody(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        this.body = json.toString();
    }

    private Map<String, String> generateDefaultHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        return Collections.unmodifiableMap(new HashMap<>(headers));
    }

}
