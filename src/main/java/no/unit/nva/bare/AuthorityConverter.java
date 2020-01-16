package no.unit.nva.bare;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthorityConverter {

    public static final String MARC_TAG_PERSONAL_NAME_FIELD_CODE = "100";
    public static final String MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE = "a";
    public static final String MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE = "d";
    public static final ValidIdentifierSource FEIDE_KEY = ValidIdentifierSource.feide;
    public static final ValidIdentifierSource ORCID_KEY = ValidIdentifierSource.orcid;
    public static final ValidIdentifierSource ORGUNITID_KEY = ValidIdentifierSource.orgunitid;
    public static final ValidIdentifierSource HANDLE_KEY = ValidIdentifierSource.handle;
    public static final String EMPTY_STRING = "";


    protected List<Authority> extractAuthoritiesFrom(InputStreamReader reader) {
        final BareQueryResponse bareQueryResponse = new Gson().fromJson(reader, BareQueryResponse.class);
        System.out.println(bareQueryResponse);
        return Arrays.stream(bareQueryResponse.results).map(this::asAuthority).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected Authority asAuthority(BareAuthority bareAuthority) {
        final String name = this.findValueIn(bareAuthority, MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE);
        final String date = this.findValueIn(bareAuthority, MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE);
        final String id = bareAuthority.systemControlNumber;
        Optional<List<String>> feideArray = Optional.ofNullable(bareAuthority.getIdentifiers(FEIDE_KEY));
        Optional<List<String>> orcIdArray = Optional.ofNullable(bareAuthority.getIdentifiers(ORCID_KEY));
        Optional<List<String>> orgUnitIdArray = Optional.ofNullable(bareAuthority.getIdentifiers(ORGUNITID_KEY));
        Optional<List<String>> handleArray = Optional.ofNullable(bareAuthority.getIdentifiers(HANDLE_KEY));
        Authority authority = new Authority();
        authority.setName(name);
        authority.setBirthDate(date);
        authority.setSystemControlNumber(id);
        authority.setFeideids(feideArray.orElse(Collections.EMPTY_LIST));
        authority.setOrcids(orcIdArray.orElse(Collections.EMPTY_LIST));
        authority.setOrgunitids(orgUnitIdArray.orElse(Collections.EMPTY_LIST));
        authority.setHandles(handleArray.orElse(Collections.EMPTY_LIST));
        return authority;
    }

    protected String findValueIn(BareAuthority bareAuthority, String marcSubfieldTag) {
        List<String> values = Arrays.stream(bareAuthority.marcdata)
                .filter(marc -> Arrays.asList(new String[]{MARC_TAG_PERSONAL_NAME_FIELD_CODE}).contains(marc.tag))
                .flatMap(marc -> Arrays.stream(marc.subfields))
                .filter(subfield -> marcSubfieldTag.equals(subfield.subcode))
                .map(subfield -> subfield.value)
                .collect(Collectors.toList());
        return !values.isEmpty() ? values.get(0) : EMPTY_STRING;
    }

}
