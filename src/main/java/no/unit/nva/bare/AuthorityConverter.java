package no.unit.nva.bare;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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


    protected Authority extractAuthorityFrom(Reader reader) {
        final BareAuthority bareAuthority = new Gson().fromJson(reader, BareAuthority.class);
        System.out.println(bareAuthority);
        return asAuthority(bareAuthority);
    }

    protected List<Authority> extractAuthoritiesFrom(InputStreamReader reader) {
        final BareQueryResponse bareQueryResponse = new Gson().fromJson(reader, BareQueryResponse.class);
        System.out.println(bareQueryResponse);
        return Arrays.stream(bareQueryResponse.results).map(this::asAuthority).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Authority asAuthority(BareAuthority bareAuthority) {
        final String name = this.findValueIn(bareAuthority, MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE);
        final String date = this.findValueIn(bareAuthority, MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE);
        final String id = bareAuthority.systemControlNumber;
        Optional<List<String>> feideArray = Optional.ofNullable(bareAuthority.getIdentifiers(FEIDE_KEY));
        Optional<List<String>> orcIdArray = Optional.ofNullable(bareAuthority.getIdentifiers(ORCID_KEY));
        Optional<List<String>> handleArray = Optional.ofNullable(bareAuthority.getIdentifiers(HANDLE_KEY));
        Authority authority = new Authority();
        authority.setName(name);
        authority.setBirthDate(date);
        authority.setSystemControlNumber(id);
        authority.setFeideids(feideArray.orElse(Collections.EMPTY_LIST));
        authority.setOrcids(orcIdArray.orElse(Collections.EMPTY_LIST));
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
