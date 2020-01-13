package no.unit.nva.bare;

import com.google.gson.annotations.SerializedName;

public class BareResponse {
    @SerializedName(value = "q", alternate = "query")
    public String query;
    public String startRow;
    public String maxRows;
    public int numFound;
    public BareAuthority[] results;

    public String getQuery() {
        return query;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    @Override
    public String toString() {
        return "BareResponse{"
                + "query='"
                + getQuery() + '\''
                + ", numFound="
                + getNumFound()
                + '}';
    }
}
