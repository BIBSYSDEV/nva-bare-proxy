package no.unit.nva.bare;

public class Authority {

    private String name;
    private String scn;
    private String feideId;
    private String orcId;
    private String birthDate;
    private String handle;

    public Authority() {
        name = "";
        scn = "";
        feideId = "";
        orcId = "";
        birthDate = "";
        handle = "";
    }

    public String getName() {
        return name;
    }

    public String getFeideId() {
        return feideId;
    }

    public String getOrcId() {
        return orcId;
    }

    public String getScn() {
        return scn;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getHandle() {
        return handle;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScn(String scn) {
        this.scn = scn;
    }

    public void setFeideId(String feideId) {
        this.feideId = feideId;
    }

    public void setOrcId(String orcId) {
        this.orcId = orcId;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
