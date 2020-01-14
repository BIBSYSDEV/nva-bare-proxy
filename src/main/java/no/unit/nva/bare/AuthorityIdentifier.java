package no.unit.nva.bare;

public class AuthorityIdentifier {

    public static final String EMPTY_STRING = "";
    private String source;
    private String identifier;

    /**
     * POJO to hold authority identifier.
     */
    public AuthorityIdentifier() {
        source = EMPTY_STRING;
        identifier = EMPTY_STRING;
    }

    public AuthorityIdentifier(String source, String identifier) {
        this.source = source;
        this.identifier = identifier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
