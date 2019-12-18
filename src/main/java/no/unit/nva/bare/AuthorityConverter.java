package no.unit.nva.bare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthorityConverter {


    public static final String NUM_FOUND_TAG = "numFound";
    public static final String RESULTS_TAG = "results";
    public static final String MARCDATA_TAG = "marcdata";
    public static final String MARC_TAG_PERSONAL_NAME_FIELD_CODE = "100";
    public static final String MARC_TAG_TAG = "tag";
    public static final String MARC_TAG_SUBFIELDS = "subfields";
    public static final String MARC_TAG_SUBCODE = "subcode";
    public static final String MARC_TAG_VALUE = "value";
    public static final String MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE = "a";
    public static final String MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE = "d";
    public static final String IDENTIFIERS_MAP_KEY = "identifiersMap";
    public static final String SCN_KEY = "scn";
    public static final String FEIDE_KEY = "feide";
    public static final String ORCID_KEY = "orcid";
    public static final String EMPTY_STRING = "";

    protected List<Authority> getAuthoritiesFrom(JsonObject jsonObject) {
        int numFound = jsonObject.get(NUM_FOUND_TAG).getAsInt();
        List<Authority> authorityList = new ArrayList<>(numFound);
        final JsonArray results = jsonObject.get(RESULTS_TAG).getAsJsonArray();
        for (JsonElement jsonElement : results) {
            final JsonObject result = (JsonObject) jsonElement;
            // Todo: decide if we should keep that hack to silence pmd AvoidInstantiatingObjectsInLoops-rule
            //          or better an exclude the rule
            Authority authority = this.createNewAuthority();
            final JsonArray marcdata = result.get(MARCDATA_TAG).getAsJsonArray();
            for (JsonElement marcdatum : marcdata) {
                final JsonObject marc = (JsonObject) marcdatum;
                if (MARC_TAG_PERSONAL_NAME_FIELD_CODE.equals(marc.get(MARC_TAG_TAG).getAsString())) {
                    JsonArray subfields = marc.get(MARC_TAG_SUBFIELDS).getAsJsonArray();
                    for (JsonElement subfield : subfields) {
                        JsonObject sub = (JsonObject) subfield;
                        String subcodeTag = sub.get(MARC_TAG_SUBCODE).getAsString();
                        if (MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE.equals(subcodeTag)) {
                            authority.setName(sub.get(MARC_TAG_VALUE).getAsString());
                        }
                        if (MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE.equals(subcodeTag)) {
                            authority.setBirthDate(sub.get(MARC_TAG_VALUE).getAsString());
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

    private Authority createNewAuthority() {
        return new Authority();
    }

    protected String getValueFromJsonArray(JsonObject jsonObject, String key) {
        String value = EMPTY_STRING;
        JsonElement jsonElement = jsonObject.get(key);
        if (isNonEmptyArray(jsonElement)) {
            value = jsonElement.getAsJsonArray().get(0).getAsString();
        }
        return value;
    }

    private boolean isNonEmptyArray(JsonElement jsonElement) {
        return Objects.nonNull(jsonElement) && jsonElement.getAsJsonArray().size() > 0;
    }
}
