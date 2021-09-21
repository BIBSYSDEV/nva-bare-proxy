package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

public class Authority implements JsonSerializable {

    public static final String EMPTY_STRING = null;
    private static final URI UNDEFINED_ID = null;
    @JsonProperty("feideids")
    private final Set<String> feideids;
    @JsonProperty("orcids")
    private final Set<String> orcids;
    @JsonProperty("orgunitids")
    private final Set<String> orgunitids;
    @JsonProperty("handles")
    private final Set<String> handles;
    @JsonProperty("id")
    private URI id;
    @JsonProperty("name")
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String systemControlNumber;
    @JsonProperty("birthDate")
    private String birthDate;

    /**
     * POJO to hold authority metadata.
     */
    public Authority() {
        id = UNDEFINED_ID;
        name = EMPTY_STRING;
        systemControlNumber = EMPTY_STRING;
        feideids = new HashSet<>();
        orcids = new HashSet<>();
        orgunitids = new HashSet<>();
        birthDate = EMPTY_STRING;
        handles = new HashSet<>();
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getFeideids() {
        return feideids;
    }

    public void setFeideids(Collection<String> feideids) {
        this.feideids.addAll(feideids);
    }

    public Set<String> getOrcids() {
        return orcids;
    }

    public void setOrcids(Collection<String> orcids) {
        this.orcids.addAll(orcids);
    }

    public Set<String> getOrgunitids() {
        return orgunitids;
    }

    public void setOrgunitids(Collection<String> orgunitids) {
        this.orgunitids.addAll(orgunitids);
    }

    @JsonIgnore
    public String getSystemControlNumber() {
        return systemControlNumber;
    }

    public void setSystemControlNumber(String systemControlNumber) {
        this.systemControlNumber = systemControlNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Set<String> getHandles() {
        return handles;
    }

    public void setHandles(Collection<String> handles) {
        this.handles.addAll(handles);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(),
                            getName(),
                            getSystemControlNumber(),
                            getFeideids(),
                            getOrcids(),
                            getOrgunitids(),
                            getBirthDate(),
                            getHandles());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authority)) {
            return false;
        }
        Authority authority = (Authority) o;

        return Objects.equals(getId(), authority.getId())
               && Objects.equals(getName(), authority.getName())
               && Objects.equals(getSystemControlNumber(), authority.getSystemControlNumber())
               && Objects.equals(getFeideids(), authority.getFeideids())
               && Objects.equals(getOrcids(), authority.getOrcids())
               && Objects.equals(getOrgunitids(), authority.getOrgunitids())
               && Objects.equals(getBirthDate(), authority.getBirthDate())
               && Objects.equals(getHandles(), authority.getHandles());
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return toJsonString();
    }
}
