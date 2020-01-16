package no.unit.nva.bare;

public enum ValidIdentifierSource {

    FEIDE("feide"),
    ORCID("orcid"),
    ORGUNITID("orgunitid"),
    HANDLE("handle");

    private final String source;


    ValidIdentifierSource(String source) {
        this.source = source;
    }

    public String asString() {
        return source;
    }

}
