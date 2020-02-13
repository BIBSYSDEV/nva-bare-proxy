package no.unit.nva.bare;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;


public class AuthorityIdentifierTest {

    public static final ValidIdentifierSource SOURCE = ValidIdentifierSource.feide;
    public static final String IDENTIFIER = "identifier";

    @Test
    public void testEmptyConstructor() throws IOException {
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier();
        authorityIdentifier.setIdentifier(IDENTIFIER);
        authorityIdentifier.setSource(SOURCE.asString());
        assertNotNull(authorityIdentifier);

        String identifier = authorityIdentifier.getIdentifier();
        final String source = authorityIdentifier.getSource();

        assertEquals(IDENTIFIER, identifier);
        assertEquals(SOURCE.asString(), source);
    }

    @Test
    public void testConstructorWithParameters() throws IOException {
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier(SOURCE.asString(), IDENTIFIER);

        assertNotNull(authorityIdentifier);

        String identifier = authorityIdentifier.getIdentifier();
        final String source = authorityIdentifier.getSource();

        assertEquals(IDENTIFIER, identifier);
        assertEquals(SOURCE.asString(), source);
    }



}
