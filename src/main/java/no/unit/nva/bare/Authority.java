package no.unit.nva.bare;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Authority {

    public static final String EMPTY_STRING = "";
    private String name;
    private String scn;
    private final Set<String> feideIds;
    private final Set<String> orcIds;
    private String birthDate;
    private final Set<String> handles;

    /**
     * POJO to hold authority metadata.
     */
    public Authority() {
        name = EMPTY_STRING;
        scn = EMPTY_STRING;
        feideIds = new HashSet<>();
        orcIds = new HashSet<>();
        birthDate = EMPTY_STRING;
        handles = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getFeideIds() {
        return feideIds;
    }

    public Set<String> getOrcIds() {
        return orcIds;
    }

    public String getScn() {
        return scn;
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

    public void setScn(String scn) {
        this.scn = scn;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setFeideIds(Collection<String> feideIds) {
        this.feideIds.addAll(feideIds);
    }

    public void setOrcIds(Collection<String> orcIds) {
        this.orcIds.addAll(orcIds);
    }

    public void setHandles(Collection<String> handles) {
        if (!(handles == null || handles.isEmpty())) {
            this.handles.addAll(handles);
        }
    }
}
