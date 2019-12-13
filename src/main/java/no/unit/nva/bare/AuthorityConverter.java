package no.unit.nva.bare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AuthorityConverter {


    public static final String NUM_FOUND_TAG = "numFound";
    public static final String RESULTS_TAG = "results";
    public static final String MARCDATA_TAG = "marcdata";
    public static final String MARC_100 = "100";
    public static final String MARC_TAG_TAG = "tag";
    public static final String MARC_SUBFIELDS_TAG = "subfields";
    public static final String MARC_SUBCODE_TAG = "subcode";
    public static final String MARC_VALUE_TAG = "value";
    public static final String MARC_A = "a";
    public static final String MARC_D = "d";
    public static final String IDENTIFIERS_MAP_KEY = "identifiersMap";
    public static final String SCN_KEY = "scn";
    public static final String FEIDE_KEY = "feide";
    public static final String ORCID_KEY = "orcid";

    protected List<Authority> getAuthoritiesFrom(JsonObject jsonObject) {
        int numFound = jsonObject.get(NUM_FOUND_TAG).getAsInt();
        List<Authority> authorityList = new ArrayList<>(numFound);
        final JsonArray results = jsonObject.get(RESULTS_TAG).getAsJsonArray();
        Authority authority;
        for (JsonElement jsonElement : results) {
            final JsonObject result = (JsonObject) jsonElement;
            authority = new Authority();
            final JsonArray marcdata = result.get(MARCDATA_TAG).getAsJsonArray();
            for (JsonElement marcdatum : marcdata) {
                final JsonObject marc = (JsonObject) marcdatum;
                if (MARC_100.equals(marc.get(MARC_TAG_TAG).getAsString())) {
                    JsonArray subfields = marc.get(MARC_SUBFIELDS_TAG).getAsJsonArray();
                    for (JsonElement subfield : subfields) {
                        JsonObject sub = (JsonObject) subfield;
                        if (MARC_A.equals(sub.get(MARC_SUBCODE_TAG).getAsString())) {
                            authority.setName(sub.get(MARC_VALUE_TAG).getAsString());
                        }
                        if (MARC_D.equals(sub.get(MARC_SUBCODE_TAG).getAsString())) {
                            authority.setBirthDate(sub.get(MARC_VALUE_TAG).getAsString());
                        }
                    }
                }
            }
            final JsonObject identifiersMap = (JsonObject) result.get(IDENTIFIERS_MAP_KEY);
            authority.setScn(getValueFromJsonArray(identifiersMap, SCN_KEY));
            authority.setFeideId(getValueFromJsonArray(identifiersMap, FEIDE_KEY));
            authority.setOrcId(getValueFromJsonArray(identifiersMap, ORCID_KEY));
            authorityList.add(authority);
        }
        return authorityList;
    }

    protected String getValueFromJsonArray(JsonObject jsonObject, String key) {
        String value = "";
        JsonElement jsonElement = jsonObject.get(key);
        if (jsonElement != null && jsonElement.getAsJsonArray().size() > 0) {
            value = jsonElement.getAsJsonArray().get(0).getAsString();
        }
        return value;
    }
}
