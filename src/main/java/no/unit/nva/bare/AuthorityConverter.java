package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthorityConverter {

    public static final String MARC_TAG_PERSONAL_NAME_FIELD_CODE = "100";
    public static final String MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE = "a";
    public static final String MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE = "d";
    public static final String SCN_KEY = "scn";
    public static final String FEIDE_KEY = "feide";
    public static final String ORCID_KEY = "orcid";
    public static final String EMPTY_STRING = "";


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

    protected List<Authority> extractAuthoritiesFrom(InputStreamReader reader) {
        final BareResponse bareResponse = new Gson().fromJson(reader, BareResponse.class);
        return Arrays.stream(bareResponse.results).map(this::asAuthority).collect(Collectors.toList());
    }

    protected List<Authority> extractAuthoritiesFrom(String json) {
        final BareResponse bareResponse = new Gson().fromJson(json, BareResponse.class);
        return Arrays.stream(bareResponse.results).map(this::asAuthority).collect(Collectors.toList());
    }

    private Authority asAuthority(BareAuthority bareAuthority) {
        final String name = this.findValueIn(bareAuthority, MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE);
        final String date = this.findValueIn(bareAuthority, MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE);
        final String id = bareAuthority.systemControlNumber;
        Optional<String[]> scnArray = Optional.ofNullable(bareAuthority.identifiersMap.get(SCN_KEY));
        final String scn = scnArray.orElse(new String[]{id})[0];
        Optional<String[]> feideArray = Optional.ofNullable(bareAuthority.identifiersMap.get(FEIDE_KEY));
        final String feideId = feideArray.orElse(new String[]{EMPTY_STRING})[0];
        Optional<String[]> orcIdArray = Optional.ofNullable(bareAuthority.identifiersMap.get(ORCID_KEY));
        final String orcId = orcIdArray.orElse(new String[]{EMPTY_STRING})[0];
        Authority authority = new Authority();
        authority.setName(name);
        authority.setScn(scn);
        authority.setFeideId(feideId);
        authority.setOrcId(orcId);
        authority.setBirthDate(date);
        return authority;
    }

    private String findValueIn(BareAuthority bareAuthority, String marcSubfieldTag) {
        List<String> values = Arrays.stream(bareAuthority.marcdata)
                .filter(marc -> Arrays.asList(new String[]{MARC_TAG_PERSONAL_NAME_FIELD_CODE}).contains(marc.tag))
                .flatMap(marc -> Arrays.stream(marc.subfields))
                .filter(subfield -> marcSubfieldTag.equals(subfield.subcode))
                .map(subfield -> subfield.value)
                .collect(Collectors.toList());
        return !values.isEmpty() ? values.get(0) : EMPTY_STRING;
    }


}
