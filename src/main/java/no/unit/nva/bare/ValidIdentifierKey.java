package no.unit.nva.bare;

public enum ValidIdentifierKey {

    FEIDEID("feideid"),
    ORCID("orcid"),
    ORGUNITID("orgunitid");

    private final String source;

    ValidIdentifierKey(String source) {
        this.source = source;
    }

    public String asString() {
        return source;
    }
}
