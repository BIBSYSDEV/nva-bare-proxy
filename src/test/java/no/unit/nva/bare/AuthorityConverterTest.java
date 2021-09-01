package no.unit.nva.bare;

import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorityConverterTest {


    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String CREATE_AUTHORITY_REQUEST_TO_BARE_JSON =
            "createAuthorityRequestToBare.json";
    public static final String INVERTED_NAME = "Unit, DotNo";
    public static final String HTTPS_LOCALHOST_PERSON = "https://localhost/person/";



    /**
     * Initialize tests.
     */
    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testEmptyBareAuthority() throws IOException {
        InputStream streamResp =
                IoUtils.inputStreamFromResources(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        AuthorityConverter authorityConverter = new AuthorityConverter();
        final BareAuthority bareAuthority = objectMapperWithEmpty.readValue(new InputStreamReader(streamResp), BareAuthority.class);
        final String value = authorityConverter.findValueIn(bareAuthority, "whatEver");
        assertEquals("", value);
    }

    @Test
    public void testBuildAuthority() throws IOException {
        InputStream streamResp = IoUtils.inputStreamFromResources(CREATE_AUTHORITY_REQUEST_TO_BARE_JSON);
        final BareAuthority expectedAuth = objectMapperWithEmpty.readValue(new InputStreamReader(streamResp), BareAuthority.class);
        AuthorityConverter authorityConverter = new AuthorityConverter();
        final BareAuthority bareAuthority = authorityConverter.buildAuthority(INVERTED_NAME);
        assertEquals(expectedAuth.getStatus(), bareAuthority.getStatus());
        assertEquals(expectedAuth.getAuthorityType(), bareAuthority.getAuthorityType());
        assertEquals(expectedAuth.getMarcdata()[0].getSubfields()[0].getValue(),
                bareAuthority.getMarcdata()[0].getSubfields()[0].getValue());

        assertEquals(expectedAuth, bareAuthority);
    }
}
