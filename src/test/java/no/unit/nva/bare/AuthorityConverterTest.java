package no.unit.nva.bare;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class AuthorityConverterTest {


    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";

    @Test
    public void testEmptyBareAuthority() {
        InputStream streamResp = AuthorityConverterTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        AuthorityConverter authorityConverter = new AuthorityConverter();
        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(streamResp), BareAuthority.class);
        final String value = authorityConverter.findValueIn(bareAuthority, "whatEver");
        assertEquals("", value);
    }
}
