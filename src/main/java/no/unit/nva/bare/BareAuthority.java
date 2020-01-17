package no.unit.nva.bare;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BareAuthority {
    public String systemControlNumber;
    public Marc21[] marcdata;
    private Map<String, List<String>> identifiersMap;

    public Map<String, List<String>> getIdentifiersMap() {
        return identifiersMap;
    }

    public String getSystemControlNumber() {
        return systemControlNumber;
    }

    public void setSystemControlNumber(String systemControlNumber) {
        this.systemControlNumber = systemControlNumber;
    }

//    public Marc21[] getMarcdata() {
//        return marcdata;
//    }
//
//    public void setMarcdata(Marc21[] marcdata) {
//        this.marcdata = marcdata;
//    }

    public void setIdentifiersMap(Map<String, List<String>> identifiersMap) {
        this.identifiersMap = identifiersMap;
    }

    /**
     * Checks if identifier is present for given system.
     *
     * @param authorityIdentifier identifier of authority
     * @return <code>TRUE</code> if identifier exists
     */
    public boolean hasIdentifier(AuthorityIdentifier authorityIdentifier) {
        if (Objects.nonNull(identifiersMap) && identifiersMap.containsKey(authorityIdentifier.getSource())) {
            return identifiersMap.get(authorityIdentifier.getSource()).contains(authorityIdentifier.getIdentifier());
        }
        return false;
    }

    /**
     * Help method to get identifiers.
     * @param source key for identifying system
     * @return List of identifiers
     */
    public List<String> getIdentifiers(ValidIdentifierSource source) {
        if (Objects.nonNull(identifiersMap) && identifiersMap.containsKey(source.asString())) {
            return identifiersMap.get(source.asString());
        }
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "BareAuthority{"
                + "systemControlNumber='" + systemControlNumber + '\''
                + ", identifiersMap=" + identifiersMap + '}';
    }
}
