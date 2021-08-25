package no.unit.nva.bare;

import static nva.commons.core.JsonUtils.objectMapper;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    public static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String EMPTY_JSON = "{}";
    public static final transient String ERROR_KEY = "error";
    private final transient Logger logger = Logger.instance();
    private String body;
    private transient Map<String, String> headers;
    private int statusCode;

    /**
     * GatewayResponse contains response status, response headers and body with payload resp. error messages.
     */
    public GatewayResponse() {
        this.statusCode = SC_INTERNAL_SERVER_ERROR;
        this.body = EMPTY_JSON;
        this.generateDefaultHeaders();
    }

    /**
     * GatewayResponse convenience constructor to set response status and body with payload direct.
     */
    public GatewayResponse(final String body, final int status) {
        this.statusCode = status;
        this.body = body;
        this.generateDefaultHeaders();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
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
        Map<String, String> bodyContent = new ConcurrentHashMap<>();
        bodyContent.put(ERROR_KEY, message);
        try {
            this.body = objectMapper.writeValueAsString(bodyContent);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
    }

    private void generateDefaultHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        final String corsAllowDomain = Config.getInstance().getCorsHeader();
        if (StringUtils.isNotEmpty(corsAllowDomain)) {
            headers.put(CORS_ALLOW_ORIGIN_HEADER, corsAllowDomain);
        }
        this.headers = Collections.unmodifiableMap(new ConcurrentHashMap<>(headers));
    }

}
