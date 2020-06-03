package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AuthorityResponse {

    public static final String EMPTY_STRING = "";

    @JsonProperty("name")
    private String name;

    @JsonProperty("systemControlNumber")
    private String systemControlNumber;

    @JsonProperty("feideids")
    private final Set<String> feideids;

    @JsonProperty("orcids")
    private final Set<String> orcids;

    @JsonProperty("orgunitids")
    private final Set<String> orgunitids;

    @JsonProperty("birthDate")
    private String birthDate;

    @JsonProperty("handles")
    private final Set<String> handles;

    /**
     * POJO to hold authority metadata.
     */
    public AuthorityResponse() {
        name = EMPTY_STRING;
        systemControlNumber = EMPTY_STRING;
        feideids = new HashSet<>();
        orcids = new HashSet<>();
        orgunitids = new HashSet<>();
        birthDate = EMPTY_STRING;
        handles = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getFeideids() {
        return feideids;
    }

    public Set<String> getOrcids() {
        return orcids;
    }

    public Set<String> getOrgunitids() {
        return orgunitids;
    }

    public String getSystemControlNumber() {
        return systemControlNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public Set<String> getHandles() {
        return handles;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSystemControlNumber(String systemControlNumber) {
        this.systemControlNumber = systemControlNumber;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setFeideids(Collection<String> feideids) {
        this.feideids.addAll(feideids);
    }

    public void setOrcids(Collection<String> orcids) {
        this.orcids.addAll(orcids);
    }

    public void setOrgunitids(Collection<String> orgunitids) {
        this.orgunitids.addAll(orgunitids);
    }

    public void setHandles(Collection<String> handles) {
        this.handles.addAll(handles);
    }
}
