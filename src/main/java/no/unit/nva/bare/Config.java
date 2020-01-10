package no.unit.nva.bare;

import org.apache.commons.lang3.StringUtils;

public class Config {

    public static final String MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";
    public static final String CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME = "AllowOrigin";
    public static final String BARE_APIKEY_KEY = "BARE_API_KEY";
    public static final String BARE_HOST_KEY = "BARE_HOST";
    private String bareHost;
    private String bareApikey;
    private String corsHeader;

    public static final String BARE_PATH = "/authority/rest/functions/v2/query";

    private Config() {
    }

    private static class LazyHolder {

        private static final Config INSTANCE = new Config();

        static {
            INSTANCE.setBareApikey(System.getenv(BARE_APIKEY_KEY));
            INSTANCE.setBareHost(System.getenv(BARE_HOST_KEY));
            INSTANCE.setCorsHeader(System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME));
        }
    }

    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Checking if bareHost and bareApiKey are present.
     * @return true if both properties are present.
     */
    public boolean checkProperties() {
        if (StringUtils.isEmpty(bareHost) || StringUtils.isEmpty(bareApikey)) {
            throw new RuntimeException(MISSING_ENVIRONMENT_VARIABLES);
        }
        return true;
    }

    public String getBareHost() {
        return bareHost;
    }

    public void setBareHost(String bareHost) {
        this.bareHost = bareHost;
    }

    public String getBareApikey() {
        return bareApikey;
    }

    public void setBareApikey(String bareApikey) {
        this.bareApikey = bareApikey;
    }

    public String getCorsHeader() {
        return corsHeader;
    }

    public void setCorsHeader(String corsHeader) {
        this.corsHeader = corsHeader;
    }

}
