package no.unit.nva.bare;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BareConnection {

    public static final String HTTPS = "https";
    public static final String BARE_HOST = System.getenv("bareHost");
    public static final String BARE_PATH = "/authority/rest/functions/v2/query";
    public static final String BARE_APIKEY = System.getenv("bareApiKey");
    public static final String APIKEY_KEY = "apikey";
    private transient final CloseableHttpClient httpClient;

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
            throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        final String authoritytype = " authoritytype:person";
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(BARE_HOST)
                .setPath(BARE_PATH)
                .setParameter("q", URLEncoder.encode(authorityName + authoritytype, StandardCharsets.UTF_8.toString()))
                .setParameter("start", "1")
                .setParameter("max", "10")
                .setParameter("format", "json")
                .build();
        return uri.toURL();
    }

    public CloseableHttpResponse update(Authority authority) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(BARE_HOST)
                .setPath(BARE_PATH)
                .setPath(authority.getScn())
                .build();
        HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeader(APIKEY_KEY, BARE_APIKEY);
        putRequest.setEntity(new StringEntity(new Gson().toJson(authority, Authority.class)));
        return httpClient.execute(putRequest);
    }
}
