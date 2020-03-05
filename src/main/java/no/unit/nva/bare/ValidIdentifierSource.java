package no.unit.nva.bare;

public enum ValidIdentifierSource {

    feide("feide"),
    orcid("orcid"),
    orgunitid("orgunitid"),
    handle("handle");

    private final String source;


    ValidIdentifierSource(String source) {
        this.source = source;
    }

    public String asString() {
        return source;
    }

}
