package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
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
    public static final String HANDLE_KEY = "handle";
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
        return Objects.nonNull(jsonElement) && jsonElement.isJsonArray();
    }

    protected List<Authority> extractAuthoritiesFrom(InputStreamReader reader) {
        final BareResponse bareResponse = new Gson().fromJson(reader, BareResponse.class);
        System.out.println(bareResponse);
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
        Optional<List<String>> feideArray = Optional.ofNullable(bareAuthority.identifiersMap.get(FEIDE_KEY));
        Optional<List<String>> orcIdArray = Optional.ofNullable(bareAuthority.identifiersMap.get(ORCID_KEY));
        Optional<List<String>> handleArray = Optional.ofNullable(bareAuthority.identifiersMap.get(HANDLE_KEY));
        Authority authority = new Authority();
        authority.setName(name);
        authority.setBirthDate(date);
        authority.setScn(id);
        authority.setFeideIds(feideArray.orElse(Collections.EMPTY_LIST));
        authority.setOrcIds(orcIdArray.orElse(Collections.EMPTY_LIST));
        List handles = handleArray.orElse(Collections.EMPTY_LIST);
        authority.setHandles(handles);
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
