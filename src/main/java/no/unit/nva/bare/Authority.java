package no.unit.nva.bare;

public class Authority {

    private String name;
    private String scn;
    private String feideId;
    private String orcId;
    private String birthDate;

    public Authority() {
        name = "";
        scn = "";
        feideId = "";
        orcId = "";
        birthDate = "";
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

    public void setScn(String scn) {
        this.scn = scn;
    }

    public void setName(String name) {
        this.name = name;
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

    public static Authority create() {
        return new Authority();
    }
}
