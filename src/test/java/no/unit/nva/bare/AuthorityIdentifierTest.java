package no.unit.nva.bare;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;


public class AuthorityIdentifierTest {

    public static final String SOURCE = "source";
    public static final String IDENTIFIER = "identifier";

    @Test
    public void testEmptyConstructor() throws IOException {
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier();
        authorityIdentifier.setIdentifier(IDENTIFIER);
        authorityIdentifier.setSource(SOURCE);
        assertNotNull(authorityIdentifier);

        String identifier = authorityIdentifier.getIdentifier();
        final String source = authorityIdentifier.getSource();

        assertEquals(IDENTIFIER, identifier);
        assertEquals(SOURCE, source);
    }

    @Test
    public void testConstructorWithParameters() throws IOException {
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier(SOURCE, IDENTIFIER);

        assertNotNull(authorityIdentifier);

        String identifier = authorityIdentifier.getIdentifier();
        final String source = authorityIdentifier.getSource();

        assertEquals(IDENTIFIER, identifier);
        assertEquals(SOURCE, source);
    }



}

