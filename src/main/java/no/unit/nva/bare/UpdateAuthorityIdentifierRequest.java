package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateAuthorityIdentifierRequest {

    private final String identifier;
    private final String updatedIdentifier;

    /**
     * Creates a request to update identifier.
     *
     * @param identifier        current value of identifier
     * @param updatedIdentifier new value of identifier
     */
    @JsonCreator
    public UpdateAuthorityIdentifierRequest(@JsonProperty("identifier") String identifier,
                                            @JsonProperty("updatedIdentifier") String updatedIdentifier) {
        this.identifier = identifier;
        this.updatedIdentifier = updatedIdentifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getUpdatedIdentifier() {
        return updatedIdentifier;
    }

}
