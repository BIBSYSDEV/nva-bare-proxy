package no.unit.nva.bare;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD")
public class BareAuthority {

    private String authorityType = "PERSON";
    private String status;
    private String systemControlNumber;
    private Marc21[] marcdata;
    private Map<String, List<String>> identifiersMap;


    public void setAuthorityType(String authorityType) {
        this.authorityType = authorityType;
    }

    public String getAuthorityType() {
        return authorityType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Marc21[] getMarcdata() {
        return marcdata;
    }

    public void setMarcdata(Marc21[] marcdata) {
        this.marcdata = marcdata;
    }

    public Map<String, List<String>> getIdentifiersMap() {
        return identifiersMap;
    }

    public void setIdentifiersMap(Map<String, List<String>> identifiersMap) {
        this.identifiersMap = identifiersMap;
    }

    public String getSystemControlNumber() {
        return systemControlNumber;
    }

    public void setSystemControlNumber(String systemControlNumber) {
        this.systemControlNumber = systemControlNumber;
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
     *
     * @param source key for identifying system
     * @return List of identifiers
     */
    public List<String> getIdentifiers(ValidIdentifierSource source) {
        if (Objects.nonNull(identifiersMap) && identifiersMap.containsKey(source.asString())) {
            return identifiersMap.get(source.asString());
        }
        return Collections.emptyList();
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BareAuthority)) {
            return false;
        }
        BareAuthority that = (BareAuthority) o;
        return Objects.equals(authorityType, that.authorityType)
            && Objects.equals(status, that.status)
            && Objects.equals(getSystemControlNumber(), that.getSystemControlNumber())
            && Arrays.equals(marcdata, that.marcdata)
            && Objects.equals(getIdentifiersMap(), that.getIdentifiersMap());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        int result = Objects.hash(authorityType, status, getSystemControlNumber(), getIdentifiersMap());
        result = 31 * result + Arrays.hashCode(marcdata);
        return result;
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return "BareAuthority{"
             + "authorityType='" + authorityType + '\''
             + ", status='" + status + '\''
             + ", systemControlNumber='" + systemControlNumber + '\''
             + ", marcdata=" + Arrays.toString(marcdata)
             + ", identifiersMap=" + identifiersMap
             + '}';
    }
}