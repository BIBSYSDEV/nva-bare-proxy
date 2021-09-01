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
    public static final String QUERY_PERSON_AUTHORITIES = "q=%s+authoritytype:person&start=1&max=10&format=json";
    public static final String QUERY_SPECIFY_AUTHORITY_IDENTIFIER = "identifier=%s";
    public static final String PATH_TO_AUTHORITY_TEMPLATE = "/authority/rest/authorities/v2/%s";
    public static final String DELETE_AUTHORITY_IDENTIFIER_PATH = "/authority/rest/authorities/v2/%s/identifiers/%s";
    public static final Duration TIMEOUT_DURATION = Duration.ofSeconds(15);

    public static final String ADD_NEW_AUTHORITY_IDENTIFIER_PATH = "/authority/rest/authorities/v2/%s/identifiers";
    private static final String GET_AUTHORITY_QUERY_PARAMETERS = "format=json";
    private static final String EMPTY_QUERY = null;
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
        final URI getUri = formatGetByScnQuery(systemControlNumber);

        HttpResponse<String> response = sendGetRequest(getUri);
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
    public HttpResponse<String> addNewIdentifier(String authoritySystemControlNumber,
                                                 AuthorityIdentifier authorityIdentifier)
        throws IOException,
               URISyntaxException, InterruptedException {
        String addIdentifierPath =
            String.format(ADD_NEW_AUTHORITY_IDENTIFIER_PATH, authoritySystemControlNumber);
        URI uri = new URI(HTTPS, BARE_HOST, addIdentifierPath, EMPTY_QUERY, EMPTY_FRAGMENT);

        final String body = objectMapperWithEmpty.writeValueAsString(authorityIdentifier);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();

        return sendRequest(request);
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

        URI uri = new URI(HTTPS, BARE_HOST, BARE_CREATE_PATH, EMPTY_QUERY, EMPTY_FRAGMENT);
        final String payload = bareAuthority.toJsonString();

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(payload);

        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();
        return sendRequest(request);
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
        String qualifierPath = String.format(DELETE_AUTHORITY_IDENTIFIER_PATH, systemControlNumber, qualifier);
        String deleteIdentifierQuery = String.format(QUERY_SPECIFY_AUTHORITY_IDENTIFIER, identifier);
        URI uri = new URI(HTTPS, BARE_HOST, qualifierPath, deleteIdentifierQuery, EMPTY_FRAGMENT);
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(uri);
        HttpRequest request = requestBuilder.DELETE().build();
        return sendRequest(request);
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
        return addNewIdentifier(systemControlNumber, authorityIdentifier);
    }

    protected BareQueryResponse searchByAuthorityName(String authorityName)
        throws IOException, URISyntaxException, InterruptedException {
        String query = String.format(QUERY_PERSON_AUTHORITIES, authorityName);
        URI queryUri = new URI(HTTPS, BARE_HOST, BARE_QUERY_PATH, query, EMPTY_FRAGMENT);
        HttpResponse<String> response = sendGetRequest(queryUri);
        String json = response.body();
        return JsonUtils.objectMapperWithEmpty.readValue(json, BareQueryResponse.class);
    }

    private URI formatGetByScnQuery(String systemControlNumber) throws URISyntaxException {
        String path = String.format(PATH_TO_AUTHORITY_TEMPLATE, systemControlNumber);
        return new URI(HTTPS, BARE_HOST, path, GET_AUTHORITY_QUERY_PARAMETERS, EMPTY_FRAGMENT);
    }

    private HttpResponse<String> sendGetRequest(URI getUri) throws IOException, InterruptedException {
        final HttpRequest.Builder requestBuilder = getHttpRequestBuilder(getUri);
        HttpRequest request = requestBuilder.GET().build();
        return sendRequest(request);
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
