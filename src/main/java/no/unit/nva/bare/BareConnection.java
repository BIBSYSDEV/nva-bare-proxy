package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.client.utils.URIBuilder;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    public static final String PATH_SEPARATOR = "/";

    private final transient HttpClient httpClient;

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

    protected URL generateQueryUrl(String authorityName)
            throws MalformedURLException, URISyntaxException {
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

    protected URI generateGetUrl(String systemControlNumber)
            throws URISyntaxException {

        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_GET_PATH + PATH_SEPARATOR + systemControlNumber)
                .setParameter("format", "json")
                .build();
        return uri;
    }

    /**
     * Get an authority from Bare by given systemControlNumber.
     *
     * @param systemControlNumber scn
     * @return InputStreamReader containing the authority payload
     * @throws IOException        some communication mishap
     * @throws URISyntaxException error in configuration
     */
    public BareAuthority get(String systemControlNumber) throws URISyntaxException, IOException, InterruptedException {
        final URI getUrl = generateGetUrl(systemControlNumber);
        System.out.println("bareConnection.get(" + getUrl + ")");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .version(HttpClient.Version.HTTP_2)
                .uri(getUrl)
                .timeout(TIMEOUT_DURATION)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == Response.Status.OK.getStatusCode()) {
            final String body = response.body();
            return new Gson().fromJson(body, BareAuthority.class);
        } else {
            System.out.println("Error..? " + response.body());
            throw new IOException(response.body());
        }
    }

    /**
     * Updates metadata of the given authority to Bare.
     *
     * @param authoritySystemControlNumber Identifier of Authority to update
     * @param authorityIdentifier          New identifier pair to add to authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
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
        System.out.println("uri=" + uri);

        final String body = new Gson().toJson(authorityIdentifier, AuthorityIdentifier.class);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .timeout(TIMEOUT_DURATION)
                .build();
        System.out.println("httpPost=" + request.toString());
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Creates a new authority in Bare.
     *
     * @param bareAuthority authority to be created
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public HttpResponse<String> createAuthority(BareAuthority bareAuthority)
            throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_CREATE_PATH)
                .build();
        System.out.println(URI_LOG_STRING + uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        final String payload = new Gson().toJson(bareAuthority, BareAuthority.class);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .timeout(TIMEOUT_DURATION)
                .build();
        System.out.println("httpPost=" + request.toString());
        System.out.println("payload: " + payload);
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Add an identifier for a specific qualifier to a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier           Qualifier for identifier to add to authority
     * @param identifier          Identifier to add to authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
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
        System.out.println(URI_LOG_STRING + uri);
        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .timeout(TIMEOUT_DURATION)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Delete an identifier for a specific qualifier in a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier           Qualifier for identifier to delete from authority
     * @param identifier          Identifier to delete from authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public HttpResponse<String> deleteIdentifier(String systemControlNumber, String qualifier, String identifier)
            throws IOException,
            URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier)
                .build();
        System.out.println(URI_LOG_STRING + uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .timeout(TIMEOUT_DURATION)
                .build();
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
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public HttpResponse<String> updateIdentifier(String systemControlNumber, String qualifier, String identifier,
                                                 String updatedIdentifier) throws IOException,
            URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier, "update", updatedIdentifier)
                .build();
        System.out.println(URI_LOG_STRING + uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .version(HttpClient.Version.HTTP_2)
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .timeout(TIMEOUT_DURATION)
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
