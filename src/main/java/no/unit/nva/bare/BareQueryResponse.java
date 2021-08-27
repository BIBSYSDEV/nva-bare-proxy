package no.unit.nva.bare;

import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

public class BareQueryResponse implements JsonSerializable {

    @JsonProperty("q")
    public String query;
    public String startRow;
    public String maxRows;
    public int numFound;
    public BareAuthority[] results;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return toJsonString();
    }
}
