package no.unit.nva.bare;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BareAuthorityTest {

    public static final ValidIdentifierSource FEIDE = ValidIdentifierSource.feide;
    public static final String DUMMY = "dummy";

    @Test
    public void testGetIdentifiers() {
        BareAuthority bareAuthority = new BareAuthority();
        final List<String> feide = bareAuthority.getIdentifiers(FEIDE);
        assertNotNull(feide);
    }

    @Test
    public void testHasIdentifiers() {
        BareAuthority bareAuthority = new BareAuthority();

        final HashMap<String, List<String>> identifiersMap = new HashMap<>();
        bareAuthority.setIdentifiersMap(identifiersMap);

        final ArrayList<String> value = new ArrayList<>();
        value.add(DUMMY);
        identifiersMap.put(FEIDE.asString(), value);

        bareAuthority.setSystemControlNumber(DUMMY);
        assertNotNull(bareAuthority.getIdentifiersMap());
        assertNotNull(bareAuthority.getSystemControlNumber());


    }
}
