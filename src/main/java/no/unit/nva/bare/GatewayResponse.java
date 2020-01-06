package no.unit.nva.bare;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.bare.AuthorityProxy.X_CUSTOM_HEADER;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    public static final String EMPTY_JSON = "{}";
    private String body;
    private final Map<String, String> headers;
    private Response.Status status;

    public GatewayResponse() {
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
        this.body = EMPTY_JSON;
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

    private Map<String, String> generateDefaultHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(X_CUSTOM_HEADER, MediaType.APPLICATION_JSON);
        return Collections.unmodifiableMap(new HashMap<>(headers));
    }
}
