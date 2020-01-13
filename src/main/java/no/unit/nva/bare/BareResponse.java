package no.unit.nva.bare;

import com.google.gson.annotations.SerializedName;

public class BareResponse {
    @SerializedName(value = "q", alternate = "query")
    public String query;
    public String startRow;
    public String maxRows;
    public int numFound;
    public BareAuthority[] results;

    @Override
    public String toString() {
        return "BareResponse{" +
                "query='" + query + '\'' +
                ", numFound=" + numFound +
                '}';
    }
}
