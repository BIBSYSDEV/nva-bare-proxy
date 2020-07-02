package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddNewAuthorityIdentifierRequest {

    private final String identifier;

    /**
     * Creates a request to add new identifier.
     *
     * @param identifier value of identifier
     */
    @JsonCreator
    public AddNewAuthorityIdentifierRequest(@JsonProperty("identifier") String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
