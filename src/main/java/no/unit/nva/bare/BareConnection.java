package no.unit.nva.bare;


import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.bare.Config.BARE_APIKEY;
import static no.unit.nva.bare.Config.BARE_CREATE_PATH;
import static no.unit.nva.bare.Config.BARE_HOST;
import static no.unit.nva.bare.Config.BARE_QUERY_PATH;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import nva.commons.apigateway.ContentTypes;
import nva.commons.apigateway.HttpHeaders;
import nva.commons.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BareConnection {

    public static final String HTTPS = "https";
    public static final String APIKEY_KEY = "apikey";
    public static final String SPACE = " ";
    public static final String EMPTY_FRAGMENT = null;
    public static final String QUERY_PERSON_AUTHORITIES = "?q=%s authoritytype:person&start=1&max=10&format=json";
    public static final String PATH_TO_AUTHORITY_TEMPLATE = "/authority/rest/authorities/v2/%s";
    public static final String AUTHORITY_IDENTIFIER_PATH =
        "/authority/rest/authorities/v2/%s/identifiers/%s/%s";
    public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(15);
    public static final String PATH_TO_AUTHORITY_TEMPLATE_WITH_JSON_RESULT =
        PATH_TO_AUTHORITY_TEMPLATE + "?format=json";
    public static final String ADD_NEW_AUTHORITY_IDENTIFIER_WITH_NEW_QUALIFIER_PATH =
        "/authority/rest/authorities/v2/%s/identifiers";
    private final transient HttpClient httpClient;
    private final transient Logger logger = LoggerFactory.getLogger(BareConnection.class);

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
        String path = String.format(PATH_TO_AUTHORITY_TEMPLATE_WITH_JSON_RESULT, systemControlNumber);
        final URI getUrl = new URI(HTTPS, BARE_HOST, path, EMPTY_FRAGMENT);
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(getUrl);
        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HTTP_OK) {
            final String body = response.body();
            return objectMapperWithEmpty.readValue(body, BareAuthority.class);
        } else {
            logger.error("Error..? " + response.body());
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
    public HttpResponse<String> addNewIdentifierWithNewQualifier(String authoritySystemControlNumber,
                                                                 AuthorityIdentifier authorityIdentifier)
        throws IOException,
               URISyntaxException, InterruptedException {
        String addIdentifierPath =
            String.format(ADD_NEW_AUTHORITY_IDENTIFIER_WITH_NEW_QUALIFIER_PATH, authoritySystemControlNumber);
        URI uri = new URI(HTTPS, BARE_HOST, addIdentifierPath, EMPTY_FRAGMENT);

        final String body = objectMapperWithEmpty.writeValueAsString(authorityIdentifier);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();

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

        URI uri = new URI(HTTPS, BARE_HOST, BARE_CREATE_PATH,EMPTY_FRAGMENT);
        final String payload = bareAuthority.toJsonString();

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(payload);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();
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
    public HttpResponse<String> addNewIdentifierForExistingQualifier(String systemControlNumber, String qualifier,
                                                                     String identifier)
        throws URISyntaxException, IOException, InterruptedException {

        String addIdentifierPath = createIdentifierPath(systemControlNumber, qualifier, identifier);
        URI uri = new URI(HTTPS, BARE_HOST, addIdentifierPath, EMPTY_FRAGMENT);

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

        String identifierPath = createIdentifierPath(systemControlNumber, qualifier, identifier);
        URI uri = new URI(HTTPS, BARE_HOST, identifierPath, EMPTY_FRAGMENT);

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
    public HttpResponse<String> updateIdentifier(String systemControlNumber,
                                                 String qualifier,
                                                 String identifier,
                                                 String updatedIdentifier) throws IOException, URISyntaxException,
                                                                                  InterruptedException {

        deleteIdentifier(systemControlNumber, qualifier, identifier);
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier(qualifier, updatedIdentifier);
        return addNewIdentifierWithNewQualifier(systemControlNumber, authorityIdentifier);
    }

    protected BareQueryResponse searchByAuthorityName(String authorityName)
        throws IOException, URISyntaxException, InterruptedException {
        String queryPath = String.format(BARE_QUERY_PATH + QUERY_PERSON_AUTHORITIES, authorityName);
        URI queryUri = new URI(HTTPS, BARE_HOST, queryPath, EMPTY_FRAGMENT);
        HttpRequest.Builder requestBuilder = getHttpRequestBuilder(queryUri);
        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        return JsonUtils.objectMapperWithEmpty.readValue(json, BareQueryResponse.class);
    }

    private String createIdentifierPath(String systemControlNumber, String qualifier, String identifier) {
        return String.format(AUTHORITY_IDENTIFIER_PATH, systemControlNumber, qualifier, identifier);
    }

    private HttpRequest.Builder getHttpRequestBuilder(URI uri) {
        String apiKeyAuth = APIKEY_KEY + SPACE + BARE_APIKEY;
        return HttpRequest.newBuilder()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, apiKeyAuth)
            .header(HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
            .timeout(TIMEOUT_DURATION);
    }
}
