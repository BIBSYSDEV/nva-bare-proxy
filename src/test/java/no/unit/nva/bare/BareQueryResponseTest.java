package no.unit.nva.bare;

import static no.unit.nva.bare.ApplicationConfig.defaultRestObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

public class BareQueryResponseTest {


    public static final String EMPTY_BARE_RESPONSE = "/bareEmptyResponse.json";
    public static final String AUTHORITY_SAMPLE_QUERY = "0000-0001-7884-3049 authoritytype:person";


    @Test
    public void testEmptyConstructor() {
        BareQueryResponse bareQueryResponse = new BareQueryResponse();
        assertNotNull(bareQueryResponse);
    }

    @Test
    public void testReadingFromfile() throws IOException {
        InputStream stream = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(EMPTY_BARE_RESPONSE);
        final BareQueryResponse bareQueryResponse = defaultRestObjectMapper.readValue(new InputStreamReader(stream),
                                                                           BareQueryResponse.class);
        assertNotNull(bareQueryResponse);

        assertEquals(0, bareQueryResponse.getNumFound());
        assertEquals(AUTHORITY_SAMPLE_QUERY, bareQueryResponse.getQuery());

        bareQueryResponse.setNumFound(-1);
        bareQueryResponse.setQuery(AUTHORITY_SAMPLE_QUERY);


    }



}

