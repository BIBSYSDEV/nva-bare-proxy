package no.unit.nva.bare;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BareConnection {

    private final transient String BARE_URL = "https://authority.bibsys.no/authority/rest/functions/v2/query?";
    private final transient char AMP = '&';

    public BareConnection() {
    }

    protected InputStreamReader connect(String address) throws IOException {
        URL url = new URL(address);
        return new InputStreamReader(url.openStream());
    }


    protected String setUpQueryUrl(String authorityName) throws UnsupportedEncodingException {
        final String authoritytype = " authoritytype:person";
        final String q = "q=" + URLEncoder.encode(authorityName + authoritytype, StandardCharsets.UTF_8.toString());
        final String start = "start=1";
        final String max = "max=10";
        final String format = "format=json";
        return BARE_URL + q + AMP + start + AMP + max + AMP + format;
    }

}
