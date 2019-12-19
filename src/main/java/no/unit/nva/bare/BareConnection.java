package no.unit.nva.bare;

import org.apache.http.client.utils.URIBuilder;
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
    public static final String BARE_HOST = "authority.bibsys.no";
    public static final String BARE_PATH = "/authority/rest/functions/v2/query";

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

}
