package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class BareConnection {

    public static final String HTTPS = "https";
    public static final String APIKEY_KEY = "apikey";
    public static final String URI_LOG_STRING = "uri=";
    public static final String SPACE = " ";

    public static final String PATH_SEGMENT_AUTHORITY = "authority";
    public static final String PATH_SEGMENT_REST = "rest";
    public static final String PATH_SEGMENT_AUTHORITIES = "authorities";
    public static final String PATH_SEGMENT_V_2 = "v2";
    public static final String PATH_SEGMENT_IDENTIFIERS = "identifiers";
    public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(15);

    private final transient HttpClient httpClient;
    private final transient Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final transient Logger log = Logger.instance();

    /**
     * Constructor for testability reasons.
     *
     * @param httpClient HttpClient
     */
    public BareConnection(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public BareConnection() {
        httpClient = HttpClient.newHttpClient();
    }

    protected InputStreamReader connect(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    protected URL generateQueryUrl(String authorityName) throws MalformedURLException, URISyntaxException {
        final String authoritytype = " authoritytype:person";
        String queryString = authorityName + authoritytype;
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_QUERY_PATH)
                .setParameter("q", queryString)
                .setParameter("start", "1")
                .setParameter("max", "10")
                .setParameter("format", "json")
                .build();
        return uri.toURL();
    }

    /**
     * Get an authority from Bare by given systemControlNumber.
     *
     * @param systemControlNumber scn
     * @return InputStreamReader containing the authority payload
     * @throws IOException          some communication mishap
     * @throws URISyntaxException   error in configuration
     * @throws InterruptedException error in communication
     */
    public BareAuthority get(String systemControlNumber) throws URISyntaxException, IOException, InterruptedException {
        final URI getUrl = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber)
                .setParameter("format", "json")
                .build();
        log.info("bareConnection.get(" + getUrl + ")");

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(getUrl);
        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == SC_OK) {
            final String body = response.body();
            return gson.fromJson(body, BareAuthority.class);
        } else {
            log.error("Error..? " + response.body());
            throw new IOException(response.body());
        }
    }

    /**
     * Updates metadata of the given authority to Bare.
     *
     * @param authoritySystemControlNumber Identifier of Authority to update
     * @param authorityIdentifier          New identifier pair to add to authority
     * @return CloseableHttpResponse
     * @throws IOException          communication error
     * @throws URISyntaxException   error while creating URI
     * @throws InterruptedException error in communication
     */
    public HttpResponse<String> addIdentifier(String authoritySystemControlNumber,
                                              AuthorityIdentifier authorityIdentifier) throws IOException,
            URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        authoritySystemControlNumber, PATH_SEGMENT_IDENTIFIERS)
                .build();
        log.info("uri=" + uri);

        final String body = gson.toJson(authorityIdentifier, AuthorityIdentifier.class);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();
        log.info("httpPost=" + request.toString());
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Creates a new authority in Bare.
     *
     * @param bareAuthority authority to be created
     * @return CloseableHttpResponse
     * @throws IOException          communication error
     * @throws URISyntaxException   error while creating URI
     * @throws InterruptedException error in communication
     */
    public HttpResponse<String> createAuthority(BareAuthority bareAuthority)
            throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_CREATE_PATH)
                .build();
        log.info(URI_LOG_STRING + uri);

        final String payload = gson.toJson(bareAuthority, BareAuthority.class);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(payload);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();
        log.info("httpPost=" + request.toString());
        log.info("payload: " + payload);
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Add an identifier for a specific qualifier to a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier           Qualifier for identifier to add to authority
     * @param identifier          Identifier to add to authority
     * @return CloseableHttpResponse
     * @throws IOException          communication error
     * @throws URISyntaxException   error while creating URI
     * @throws InterruptedException error in communication
     */
    public HttpResponse<String> addNewIdentifier(String systemControlNumber, String qualifier, String identifier)
            throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier)
                .build();
        log.info(URI_LOG_STRING + uri);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.noBody()).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Delete an identifier for a specific qualifier in a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier           Qualifier for identifier to delete from authority
     * @param identifier          Identifier to delete from authority
     * @return CloseableHttpResponse
     * @throws IOException          communication error
     * @throws URISyntaxException   error while creating URI
     * @throws InterruptedException error in communication
     */
    public HttpResponse<String> deleteIdentifier(String systemControlNumber, String qualifier, String identifier)
            throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier)
                .build();
        log.info(URI_LOG_STRING + uri);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.DELETE().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Update an existing identifier with a specific qualifier with a new value in a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier           Qualifier for identifier to update in authority
     * @param identifier          Existing identifier in authority
     * @param updatedIdentifier   New value of existing identifier in authority
     * @return CloseableHttpResponse
     * @throws IOException          communication error
     * @throws URISyntaxException   error while creating URI
     * @throws InterruptedException error in communication
     */
    public HttpResponse<String> updateIdentifier(String systemControlNumber, String qualifier, String identifier,
                                                 String updatedIdentifier) throws IOException, URISyntaxException,
            InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS, qualifier, identifier, "update",
                        updatedIdentifier)
                .build();
        log.info(URI_LOG_STRING + uri);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.PUT(HttpRequest.BodyPublishers.noBody()).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder getHttpRequestBuilder(URI uri) {
        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        return HttpRequest.newBuilder()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType())
                .timeout(TIMEOUT_DURATION);
    }

}
