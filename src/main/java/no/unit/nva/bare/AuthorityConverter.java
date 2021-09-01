package no.unit.nva.bare;

import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Converts marc based Bare AuthorityRecord to NVA Authority entry.
 */

public class AuthorityConverter {

    public static final String AUTHORITY_INCOMING_BARE_AUTHORTY_MESSAGE =
        "AuthorityConverter.asAuthority incoming bareAuthorty=";
    public static final String CONVERTER_AS_AUTHORITY_AUTHORITY_SCN_MESSAGE =
        "AuthorityConverter.asAuthority:authority.scn={}";
    public static final String MARC_TAG_PERSONAL_NAME_FIELD_CODE = "100";
    public static final String MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE = "a";
    public static final String MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE = "d";
    public static final ValidIdentifierSource FEIDE_KEY = ValidIdentifierSource.feide;
    public static final ValidIdentifierSource ORCID_KEY = ValidIdentifierSource.orcid;
    public static final ValidIdentifierSource ORGUNITID_KEY = ValidIdentifierSource.orgunitid;
    public static final ValidIdentifierSource HANDLE_KEY = ValidIdentifierSource.handle;
    public static final String EMPTY_STRING = "";
    public static final String BLANK = " ";
    public static final String KAT1 = "kat1";
    public static final String IND_1 = "1";
    public static final String MARCTAG_100 = "100";
    public static final String SUBCODE_A = "a";

    public static final String PATH_SEPARATOR = "/";
    private static final String EMPTY_QUERY = null;
    private static final String EMPTY_FRAGMENT = null;
    private final transient Logger logger = LoggerFactory.getLogger(AuthorityConverter.class);


    protected List<Authority> extractAuthorities(BareQueryResponse bareQueryResponse) throws IOException {
        return Arrays.stream(bareQueryResponse.results).map(this::asAuthority).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected Authority asAuthority(BareAuthority bareAuthority) {
        logger.info(AUTHORITY_INCOMING_BARE_AUTHORTY_MESSAGE + bareAuthority);
        final String name = this.findValueIn(bareAuthority, MARC_TAG_PERSONAL_NAME_VALUE_SUBFIELD_CODE);
        final String date = this.findValueIn(bareAuthority, MARC_TAG_DATES_ASSOCIATED_WITH_PERSONAL_NAME_SUBFIELD_CODE);
        final String scn = bareAuthority.getSystemControlNumber();
        Optional<List<String>> feideArray = Optional.ofNullable(bareAuthority.getIdentifiers(FEIDE_KEY));
        Optional<List<String>> orcIdArray = Optional.ofNullable(bareAuthority.getIdentifiers(ORCID_KEY));
        Optional<List<String>> orgUnitIdArray = Optional.ofNullable(bareAuthority.getIdentifiers(ORGUNITID_KEY));
        Optional<List<String>> handleArray = Optional.ofNullable(bareAuthority.getIdentifiers(HANDLE_KEY));
        Authority authority = new Authority();
        authority.setId(generateId(scn));
        authority.setName(name);
        authority.setBirthDate(date);
        authority.setSystemControlNumber(scn);
        authority.setFeideids(feideArray.orElse(Collections.EMPTY_LIST));
        authority.setOrcids(orcIdArray.orElse(Collections.EMPTY_LIST));
        authority.setOrgunitids(orgUnitIdArray.orElse(Collections.EMPTY_LIST));
        authority.setHandles(handleArray.orElse(Collections.EMPTY_LIST));
        logger.info(CONVERTER_AS_AUTHORITY_AUTHORITY_SCN_MESSAGE, authority.getSystemControlNumber());
        return authority;
    }

    protected BareAuthority buildAuthority(String name) {
        // TODO Should we add id 856$u
        BareAuthority authority = new BareAuthority();
        authority.setStatus(KAT1);
        Marc21 marcdata = new Marc21();
        marcdata.setTag(MARCTAG_100);
        marcdata.setInd1(IND_1);
        marcdata.setInd2(BLANK);
        Subfield subfield = new Subfield();
        subfield.setSubcode(SUBCODE_A);
        subfield.setValue(name);
        marcdata.setSubfields(new Subfield[]{subfield});
        authority.setMarcdata(new Marc21[]{marcdata});
        return authority;
    }

    protected String findValueIn(BareAuthority bareAuthority, String marcSubfieldTag) {
        List<String> values = Arrays.stream(bareAuthority.getMarcdata())
            .filter(marc -> Arrays.asList(new String[]{MARC_TAG_PERSONAL_NAME_FIELD_CODE}).contains(marc.getTag()))
            .flatMap(marc -> Arrays.stream(marc.getSubfields()))
            .filter(subfield -> marcSubfieldTag.equals(subfield.getSubcode()))
            .map(subfield -> subfield.getValue())
            .collect(Collectors.toList());
        return !values.isEmpty() ? values.get(0) : EMPTY_STRING;
    }

    private URI generateId(String scn) {
        URI hostUri = URI.create(Config.PERSON_AUTHORITY_BASE_ADDRESS);
        return appendPathToUri(hostUri, scn);
    }

    private URI appendPathToUri(URI hostUri, String path) {
        String newPath = hostUri.getPath() + PATH_SEPARATOR + path;
        return attempt(() -> new URI(hostUri.getScheme(), hostUri.getHost(), newPath, EMPTY_QUERY, EMPTY_FRAGMENT))
            .orElseThrow();
    }
}
