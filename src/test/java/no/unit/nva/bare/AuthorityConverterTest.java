package no.unit.nva.bare;

import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorityConverterTest {


    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String CREATE_AUTHORITY_REQUEST_TO_BARE_JSON =
            "createAuthorityRequestToBare.json";
    public static final String INVERTED_NAME = "Unit, DotNo";
    public static final String HTTPS_LOCALHOST_PERSON = "https://localhost/person/";
    public static final String HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH = "https://localhost/person";
    public static final String SYSTEM_CONTROL_NUMBER = "1";
    private Environment mockEnvironment;
    private static final ObjectMapper mapper = JsonUtils.objectMapper;


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
    public void testTrailingSlashInPersonAuthorityBaseAddress() throws IOException {
        Environment mockEnvironmentWithoutTrailingSlash = mock(Environment.class);
        when(mockEnvironmentWithoutTrailingSlash.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
                .thenReturn(HTTPS_LOCALHOST_PERSON_WITHOUT_TRAILING_SLASH);

        InputStream streamResp =
                IoUtils.inputStreamFromResources(Paths.get(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON));
        final BareAuthority bareAuthority = mapper.readValue(new InputStreamReader(streamResp), BareAuthority.class);
        bareAuthority.setSystemControlNumber(SYSTEM_CONTROL_NUMBER);
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironmentWithoutTrailingSlash);
        Authority authority  = authorityConverter.asAuthority(bareAuthority);
        URI expectedId = URI.create(HTTPS_LOCALHOST_PERSON + SYSTEM_CONTROL_NUMBER);
        assertEquals(expectedId, authority.getId());
    }


    @Test
    public void testEmptyBareAuthority() throws IOException {
        InputStream streamResp =
                IoUtils.inputStreamFromResources(Paths.get(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON));
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironment);
        final BareAuthority bareAuthority = mapper.readValue(new InputStreamReader(streamResp), BareAuthority.class);
        final String value = authorityConverter.findValueIn(bareAuthority, "whatEver");
        assertEquals("", value);
    }

    @Test
    public void testBuildAuthority() throws IOException {
        InputStream streamResp = IoUtils.inputStreamFromResources(Paths.get(CREATE_AUTHORITY_REQUEST_TO_BARE_JSON));
        final BareAuthority expectedAuth = mapper.readValue(new InputStreamReader(streamResp), BareAuthority.class);
        AuthorityConverter authorityConverter = new AuthorityConverter(mockEnvironment);
        final BareAuthority bareAuthority = authorityConverter.buildAuthority(INVERTED_NAME);
        assertEquals(expectedAuth.getStatus(), bareAuthority.getStatus());
        assertEquals(expectedAuth.getAuthorityType(), bareAuthority.getAuthorityType());
        assertEquals(expectedAuth.getMarcdata()[0].subfields[0].value, bareAuthority.getMarcdata()[0].subfields[0].value);

        assertEquals(expectedAuth, bareAuthority);
    }
}
