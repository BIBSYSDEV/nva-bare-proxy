package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteAuthorityIdentifierRequest {

    private final String identifier;

    /**
     * Creates a request to delete identifier.
     *
     * @param identifier        current value of identifier
     */
    @JsonCreator
    public DeleteAuthorityIdentifierRequest(@JsonProperty("identifier") String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
