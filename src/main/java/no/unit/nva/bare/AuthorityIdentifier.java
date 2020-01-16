package no.unit.nva.bare;

import java.util.Locale;

public class AuthorityIdentifier {

    public static final String EMPTY_STRING = "";
    private ValidIdentifierSource source;
    private String identifier;

    /**
     * POJO to hold authority identifier.
     */
    public AuthorityIdentifier() {
        identifier = EMPTY_STRING;
    }

    public AuthorityIdentifier(String source, String identifier) {
        this.source = ValidIdentifierSource.valueOf(source.toUpperCase(Locale.getDefault()));
        this.identifier = identifier;
    }

    public String getSource() {
        return source.asString();
    }

    public void setSource(String source) {
        this.source = ValidIdentifierSource.valueOf(source.toUpperCase(Locale.getDefault()));
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
