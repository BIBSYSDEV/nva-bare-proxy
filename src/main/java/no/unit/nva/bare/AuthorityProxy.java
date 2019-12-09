package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handler for requests to Lambda function.
 */
public class AuthorityProxy implements RequestHandler<String, Object> {

    private final transient String BARE_URL = "https://authority.bibsys.no/authority/rest/functions/v2/query?";
    private final transient char AMP = '&';

    @Override
    public Object handleRequest(final String input, final Context context) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        Gson gson = new Gson();
        Authority authority = gson.fromJson(input, Authority.class);
        try {
            final String q = "q=" + URLEncoder.encode(authority.getName(), StandardCharsets.UTF_8.toString());
            final String start = "start=1";
            final String max = "max=10";
            final String format = "format=json";
            String bareUrl = BARE_URL + q + AMP + start + AMP + max + AMP + format;
            final String pageContents = this.getPageContents(bareUrl);
            return new GatewayResponse(pageContents, headers, 200);
        } catch (IOException e) {
            return new GatewayResponse("{}", headers, 500);
        }
    }

    private String getPageContents(String address) throws IOException {
        URL url = new URL(address);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
