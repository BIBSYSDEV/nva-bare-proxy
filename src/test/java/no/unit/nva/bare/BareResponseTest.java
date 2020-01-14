package no.unit.nva.bare;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class BareResponseTest {


    public static final String EMPTY_BARE_RESPONSE = "/bareEmptyResponse.json";
    public static final String AUTHORITY_SAMPLE_QUERY = "0000-0001-7884-3049 authoritytype:person";


    @Test
    public void testEmptyConstructor() throws IOException {
        BareResponse bareResponse = new BareResponse();
        assertNotNull(bareResponse);
    }

    @Test
    public void testReadingFromfile() throws IOException {
        InputStream stream = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(EMPTY_BARE_RESPONSE);
        final BareResponse bareResponse = new Gson().fromJson(new InputStreamReader(stream), BareResponse.class);
        assertNotNull(bareResponse);

        assertEquals(0,bareResponse.getNumFound());
        assertEquals(AUTHORITY_SAMPLE_QUERY, bareResponse.getQuery());

        bareResponse.setNumFound(-1);
        bareResponse.setQuery(AUTHORITY_SAMPLE_QUERY);


    }



}

