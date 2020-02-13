package no.unit.nva.bare;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class BareQueryResponseTest {


    public static final String EMPTY_BARE_RESPONSE = "/bareEmptyResponse.json";
    public static final String AUTHORITY_SAMPLE_QUERY = "0000-0001-7884-3049 authoritytype:person";


    @Test
    public void testEmptyConstructor() {
        BareQueryResponse bareQueryResponse = new BareQueryResponse();
        assertNotNull(bareQueryResponse);
    }

    @Test
    public void testReadingFromfile()  {
        InputStream stream = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(EMPTY_BARE_RESPONSE);
        final BareQueryResponse bareQueryResponse = new Gson().fromJson(new InputStreamReader(stream),
                BareQueryResponse.class);
        assertNotNull(bareQueryResponse);

        assertEquals(0, bareQueryResponse.getNumFound());
        assertEquals(AUTHORITY_SAMPLE_QUERY, bareQueryResponse.getQuery());

        bareQueryResponse.setNumFound(-1);
        bareQueryResponse.setQuery(AUTHORITY_SAMPLE_QUERY);


    }



}

