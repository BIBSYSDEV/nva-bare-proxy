package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class BareConnection {

    public static final String HTTPS = "https";
    public static final String APIKEY_KEY = "apikey";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String URI_LOG_STRING = "uri=";
    public static final String SPACE = " ";

    public static final String PATH_SEGMENT_AUTHORITY = "authority";
    public static final String PATH_SEGMENT_REST = "rest";
    public static final String PATH_SEGMENT_AUTHORITIES = "authorities";
    public static final String PATH_SEGMENT_V_2 = "v2";
    public static final String PATH_SEGMENT_IDENTIFIERS = "identifiers";

    private final transient CloseableHttpClient httpClient;

    /**
     * Constructor for testability reasons.
     *
     * @param httpClient HttpClient
     */
    public BareConnection(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public BareConnection() {
        httpClient = HttpClients.createDefault();

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
                .setPath(Config.BARE_GET_PATH + "/" + systemControlNumber)
                .setParameter("format", "json")
                .build();
        return uri;
    }

    /**
     * Get an authority from Bare by given systemControlNumber.
     * @param systemControlNumber scn
     * @return InputStreamReader containing the authority payload
     * @throws IOException some communication mishap
     * @throws URISyntaxException error in configuration
     */
    public BareAuthority get(String systemControlNumber) throws URISyntaxException, IOException {
        final URI getUrl = generateGetUrl(systemControlNumber);
        System.out.println("bareConnection.get(" + getUrl + ")");
        HttpGet httpGet = new HttpGet(getUrl);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode()) {
                try (Reader streamReader = new InputStreamReader(response.getEntity().getContent())) {
                    BareAuthority fetchedAuthority = new Gson().fromJson(streamReader, BareAuthority.class);
                    return fetchedAuthority;
                }
            } else {
                System.out.println("Error..? " + response.getStatusLine().getReasonPhrase());
                throw new IOException(response.getStatusLine().getReasonPhrase());
            }
        }
    }

    /**
     * Updates metadata of the given authority to Bare.
     *
     * @param authoritySystemControlNumber Identifier of Authority to update
     * @param authorityIdentifier New identifier pair to add to authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public CloseableHttpResponse addIdentifier(String authoritySystemControlNumber,
                                               AuthorityIdentifier authorityIdentifier) throws IOException,
            URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        authoritySystemControlNumber, PATH_SEGMENT_IDENTIFIERS)
                .build();
        System.out.println("uri=" + uri);
        HttpPost httpPost = new HttpPost(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        httpPost.addHeader("Authorization", apiKeyAuth);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpPost.setEntity(new StringEntity(new Gson().toJson(authorityIdentifier, AuthorityIdentifier.class)));
        System.out.println("httpPost=" + httpPost);
        return httpClient.execute(httpPost);
    }

    /**
     * Creates a new authority in Bare.
     *
     * @param bareAuthority authority to be created
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public CloseableHttpResponse createAuthority(BareAuthority bareAuthority) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_CREATE_PATH)
                .build();
        System.out.println(URI_LOG_STRING + uri);
        HttpPost httpPost = new HttpPost(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        httpPost.addHeader("Authorization", apiKeyAuth);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        final String payload = new Gson().toJson(bareAuthority, BareAuthority.class);
        httpPost.setEntity(new StringEntity(payload));
        System.out.println("httpPost=" + httpPost);
        System.out.println("payload: " + payload);
        return httpClient.execute(httpPost);
    }

    /**
     * Add an identifier for a specific qualifier to a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier Qualifier for identifier to add to authority
     * @param identifier Identifier to add to authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public CloseableHttpResponse addNewIdentifier(String systemControlNumber, String qualifier, String identifier)
            throws IOException,
            URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier)
                .build();
        System.out.println(URI_LOG_STRING + uri);
        HttpPost httpPost = new HttpPost(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        httpPost.addHeader(AUTHORIZATION_HEADER, apiKeyAuth);

        return httpClient.execute(httpPost);
    }

    /**
     * Delete an identifier for a specific qualifier in a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier Qualifier for identifier to delete from authority
     * @param identifier Identifier to delete from authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public CloseableHttpResponse deleteIdentifier(String systemControlNumber, String qualifier, String identifier)
            throws IOException,
            URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier)
                .build();
        System.out.println(URI_LOG_STRING + uri);
        HttpDelete httpDelete = new HttpDelete(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        httpDelete.addHeader(AUTHORIZATION_HEADER, apiKeyAuth);

        return httpClient.execute(httpDelete);
    }

    /**
     * Update an existing identifier with a specific qualifier with a new value in a given authority in ARP.
     *
     * @param systemControlNumber System control number (identifier) of authority
     * @param qualifier Qualifier for identifier to update in authority
     * @param identifier Existing identifier in authority
     * @param updatedIdentifier New value of existing identifier in authority
     * @return CloseableHttpResponse
     * @throws IOException        communication error
     * @throws URISyntaxException error while creating URI
     */
    public CloseableHttpResponse updateIdentifier(String systemControlNumber, String qualifier, String identifier,
                                                  String updatedIdentifier) throws IOException,
            URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPathSegments(PATH_SEGMENT_AUTHORITY, PATH_SEGMENT_REST, PATH_SEGMENT_AUTHORITIES, PATH_SEGMENT_V_2,
                        systemControlNumber, PATH_SEGMENT_IDENTIFIERS,
                        qualifier, identifier, "update", updatedIdentifier)
                .build();
        System.out.println(URI_LOG_STRING + uri);
        HttpPut httpPut = new HttpPut(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        httpPut.addHeader(AUTHORIZATION_HEADER, apiKeyAuth);

        return httpClient.execute(httpPut);
    }
}
