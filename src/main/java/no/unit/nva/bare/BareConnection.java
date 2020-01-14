package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStreamReader;
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

    protected URL generateGetUrl(String systemControlNumber)
            throws MalformedURLException, URISyntaxException {

        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getBareHost())
                .setPath(Config.BARE_GET_PATH)
                .setPathSegments(systemControlNumber)
                .setParameter("format", "json")
                .build();
        return uri.toURL();
    }


    /**
     * Updates metadata of the given authority to Bare.
     *
     * @param authority Authority to update
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
        HttpPut putRequest = new HttpPut(uri);

        String apiKeyAuth = APIKEY_KEY + SPACE + Config.getInstance().getBareApikey();
        putRequest.addHeader("Authorization", apiKeyAuth);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        putRequest.setEntity(new StringEntity(new Gson().toJson(authorityIdentifier, AuthorityIdentifier.class)));
        System.out.println("putRequest=" + putRequest);
        return httpClient.execute(putRequest);
    }


}
