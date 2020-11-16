package no.unit.nva.bare;

import com.google.gson.Gson;
import no.unit.nva.testutils.TestContext;
import nva.commons.utils.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpResponse;

import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorityConverterTest {


    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String CREATE_AUTHORITY_REQUEST_TO_BARE_JSON =
            "/createAuthorityRequestToBare.json";
    public static final String INVERTED_NAME = "Unit, DotNo";
    public static final String HTTPS_LOCALHOST_PERSON = "https://localhost/person/";
    public static final String HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH = "https://localhost/person";
    public static final String SYSTEM_CONTROL_NUMBER = "1";
    private Environment mockEnvironment;


    /**
     * Initialize tests.
     */
    @BeforeEach
    public void setUp() {
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
                .thenReturn(HTTPS_LOCALHOST_PERSON);
    }


    @Test
    public void testTrailingSlashInPersonAuthorityBaseAddress() {
        Environment mockEnvironmentWithoutTrailingSlash = mock(Environment.class);
        when(mockEnvironmentWithoutTrailingSlash.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
                .thenReturn(HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH);

        InputStream streamResp = AuthorityConverterTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(streamResp), BareAuthority.class);
        bareAuthority.setSystemControlNumber(SYSTEM_CONTROL_NUMBER);
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironmentWithoutTrailingSlash);
        Authority authority  = authorityConverter.asAuthority(bareAuthority);
        URI expectedId = URI.create(HTTPS_LOCALHOST_PERSON + SYSTEM_CONTROL_NUMBER);
        assertEquals(expectedId, authority.getId());
    }


    @Test
    public void testEmptyBareAuthority() {
        InputStream streamResp = AuthorityConverterTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironment);
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(streamResp), BareAuthority.class);
        final String value = authorityConverter.findValueIn(bareAuthority, "whatEver");
        assertEquals("", value);
    }

    @Test
    public void testBuildAuthority() {
        InputStream streamResp = AuthorityConverterTest.class.getResourceAsStream(
                CREATE_AUTHORITY_REQUEST_TO_BARE_JSON);
        final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
        final BareAuthority expectedAuth = gson.fromJson(new InputStreamReader(streamResp), BareAuthority.class);
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironment);
        final BareAuthority bareAuthority = authorityConverter.buildAuthority(INVERTED_NAME);
        assertEquals(expectedAuth.status, bareAuthority.status);
        assertEquals(expectedAuth.authorityType, bareAuthority.authorityType);
        assertEquals(expectedAuth.marcdata[0].subfields[0].value, bareAuthority.marcdata[0].subfields[0].value);

        assertEquals(gson.toJson(expectedAuth), gson.toJson(bareAuthority));
    }
}
