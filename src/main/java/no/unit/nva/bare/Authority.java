package no.unit.nva.bare;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Authority {

    public static final String EMPTY_STRING = "";
    private String name;
    private String systemControlNumber;
    private final Set<String> feideids;
    private final Set<String> orcids;
    private String birthDate;
    private final Set<String> handles;

    /**
     * POJO to hold authority metadata.
     */
    public Authority() {
        name = EMPTY_STRING;
        systemControlNumber = EMPTY_STRING;
        feideids = new HashSet<>();
        orcids = new HashSet<>();
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

    public void setHandles(Collection<String> handles) {
        this.handles.addAll(handles);
    }
}
