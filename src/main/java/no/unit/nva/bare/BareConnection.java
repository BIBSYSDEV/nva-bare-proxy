package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
    public static final String SPACE = " ";
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
            }
            throw new IOException(response.getStatusLine().getReasonPhrase());
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
                .setPathSegments("authority", "rest", "authorities", "v2", authoritySystemControlNumber, "identifiers")
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


}
