package no.unit.nva.bare;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthorityConverterTest {


    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String CREATE_AUTHORITY_REQUEST_TO_BARE_JSON =
            "/createAuthorityRequestToBare.json";
    public static final String INVERTED_NAME = "Unit, DotNo";

    @Test
    public void testEmptyBareAuthority() {
        InputStream streamResp = AuthorityConverterTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        AuthorityConverter authorityConverter = new AuthorityConverter();
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
        AuthorityConverter authorityConverter = new AuthorityConverter();
        final BareAuthority bareAuthority = authorityConverter.buildAuthority(INVERTED_NAME);
        assertEquals(expectedAuth.status, bareAuthority.status);
        assertEquals(expectedAuth.authorityType, bareAuthority.authorityType);
        assertEquals(expectedAuth.marcdata[0].subfields[0].value, bareAuthority.marcdata[0].subfields[0].value);

        assertEquals(gson.toJson(expectedAuth), gson.toJson(bareAuthority));
    }
}
