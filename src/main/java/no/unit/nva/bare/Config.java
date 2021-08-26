package no.unit.nva.bare;

import nva.commons.core.Environment;

public final class Config {

    public static final Environment ENVIRONMENT = new Environment();
    public static final String MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";
    public static final String CORS_ALLOW_ORIGIN = readEnv("ALLOWED_ORIGIN");
    public static final String BARE_APIKEY = readEnv("BARE_API_KEY");
    public static final String PATH_SEPARATOR = "/";
    public static final String BARE_HOST = setupBareHost(readEnv("BARE_HOST"));
    public static final String BARE_QUERY_PATH = "/authority/rest/functions/v2/query";
    public static final String BARE_CREATE_PATH = "/authority/rest/authorities/v2";
    public static final String BARE_GET_PATH = "/authority/rest/authorities/v2";

    private Config() {
    }

    private static String setupBareHost(String bareHost) {
        return bareHost.endsWith(PATH_SEPARATOR)
                   ? bareHost.substring(0, bareHost.length() - 1)
                   : bareHost;
    }

    private static String readEnv(String envVariable) {
        return ENVIRONMENT.readEnv(envVariable);
    }
}
