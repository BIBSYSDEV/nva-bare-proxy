package no.unit.nva.bare;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    private String body;
    private final Map<String, String> headers;
    private Response.Status status;

    public GatewayResponse(final String body, final Map<String, String> headers, final Response.Status status) {
        this.status = status;
        this.body = body;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
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
}
