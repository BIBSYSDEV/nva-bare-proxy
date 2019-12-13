package no.unit.nva.bare;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BareConnection {

    private final static transient String bareUrl = "https://authority.bibsys.no/authority/rest/functions/v2/query?";
    private final static transient char amp = '&';

    protected InputStreamReader connect(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    protected URL generateQueryUrl(String authorityName) throws UnsupportedEncodingException, MalformedURLException {
        final String authoritytype = " authoritytype:person";
        final String q = "q=" + URLEncoder.encode(authorityName + authoritytype, StandardCharsets.UTF_8.toString());
        final String start = "start=1";
        final String max = "max=10";
        final String format = "format=json";
        final String address = bareUrl + q + amp + start + amp + max + amp + format;
        return new URL(address);
    }

}
