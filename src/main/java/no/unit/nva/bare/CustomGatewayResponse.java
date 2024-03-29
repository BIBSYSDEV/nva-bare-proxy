package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static no.unit.nva.bare.ApplicationConfig.defaultRestObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POJO containing response object for API Gateway.
 */
public class CustomGatewayResponse {

    public static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String EMPTY_JSON = "{}";
    public static final transient String ERROR_KEY = "error";
    private final transient Logger logger = LoggerFactory.getLogger(CustomGatewayResponse.class);
    private String body;
    private transient Map<String, String> headers;
    private int statusCode;

    /**
     * GatewayResponse contains response status, response headers and body with payload resp. error messages.
     */
    public CustomGatewayResponse() {
        this.statusCode = HTTP_INTERNAL_ERROR;
        this.body = EMPTY_JSON;
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
            this.body = defaultRestObjectMapper.writeValueAsString(bodyContent);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void generateDefaultHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        final String corsAllowDomain = Config.CORS_ALLOW_ORIGIN;
        if (StringUtils.isNotEmpty(corsAllowDomain)) {
            headers.put(CORS_ALLOW_ORIGIN_HEADER, corsAllowDomain);
        }
        this.headers = Collections.unmodifiableMap(new ConcurrentHashMap<>(headers));
    }
}
