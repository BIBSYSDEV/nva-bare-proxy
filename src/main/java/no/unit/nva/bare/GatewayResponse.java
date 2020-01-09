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

    public static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME = "AllowOrigin";

    public static final String EMPTY_JSON = "{}";
    private String body;
    private final Map<String, String> headers;
    private int statusCode;

    /**
     * GatewayResponse contains response status, response headers and body with payload resp. error messages.
     */
    public GatewayResponse() {
        this.statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        this.body = EMPTY_JSON;
        this.headers = this.generateDefaultHeaders();
    }

    /**
     * GatewayResponse convinience constructor to set response status and body with payload direct.
     */
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

    public int getStatusCode() {
        return statusCode;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatusCode(int status) {
        this.statusCode = status;
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
        headers.putAll(getHeadersFromEnvironment());
        return Collections.unmodifiableMap(new HashMap<>(headers));
    }

    private Map<String, String> getHeadersFromEnvironment() {
        Map<String, String> additionalHeaders = new ConcurrentHashMap<>();
        if (System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME) != null
                && System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME).length() > 0) {
            additionalHeaders.put(CORS_ALLOW_ORIGIN_HEADER, System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME));
        }
        return additionalHeaders;
    }
}
