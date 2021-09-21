package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.bare.AuthorityConverterTest.HTTPS_LOCALHOST_PERSON;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BareConnectionTest {

    public static final String COMPLETE_SINGLE_AUTHORITY_JSON = "/completeSingleAuthority.json";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
        "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String BARE_SINGLE_AUTHORITY_CREATE_RESPONSE_JSON = "/bareSingleAuthorityCreateResponse.json";
    public static final String NONSENSE_URL = "http://iam.an.url";
    public static final String SCN = "scn";
    public static final String MOCK_NAME = "Unit, DotNo";
    private static final String MOCK_BARE_HOST = "authority.bibsys.no";

    private HttpClient mockHttpClient;
    private HttpResponse mockHttpResponse;
    private Environment mockEnvironment;

    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        mockHttpClient = mock(HttpClient.class);
        mockHttpResponse = mock(HttpResponse.class);
    }

    @Test
    public void testUpdate() throws Exception {
        InputStream streamResp = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IoUtils.streamToString(streamResp);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        AuthorityIdentifier authorityIdentifier =
            new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "feide");
        HttpResponse<String> httpResponse = mockBareConnection.addNewIdentifier(SCN,
                                                                                authorityIdentifier);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = objectMapperWithEmpty.readValue(httpResponse.body(), Authority.class);

        InputStream stream =
            AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);

        String st = IoUtils.streamToString(stream);
        List<Authority> mockAuthorityList = objectMapperWithEmpty.readValue(st, new TypeReference<List<Authority>>() {
        });
        assertEquals(mockAuthorityList.get(0).getSystemControlNumber(), updatedAuthority.getSystemControlNumber());
        assertNotNull(updatedAuthority.getFeideids());
        assertNotNull(updatedAuthority.getOrcids());
        assertNotNull(updatedAuthority.getOrgunitids());
    }

    @Test
    public void testAddNewIdentifier() throws Exception {
        InputStream streamResp = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IoUtils.streamToString(streamResp);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);
        AuthorityIdentifier authorityIdentifier =
            new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "feide");
        HttpResponse<String> httpResponse = mockBareConnection.addNewIdentifier(SCN,
                                                                                authorityIdentifier);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = objectMapperWithEmpty.readValue(httpResponse.body(), Authority.class);

        InputStream stream =
            AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IoUtils.streamToString(stream);
        List<Authority> mockAuthorityList = objectMapperWithEmpty.readValue(st, new TypeReference<List<Authority>>() {
        });
        assertEquals(mockAuthorityList.get(0).getSystemControlNumber(), updatedAuthority.getSystemControlNumber());
        assertNotNull(updatedAuthority.getFeideids());
        assertNotNull(updatedAuthority.getOrcids());
        assertNotNull(updatedAuthority.getOrgunitids());
    }

    @Test
    public void testDeleteIdentifier() throws Exception {
        InputStream streamResp = DeleteAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IoUtils.streamToString(streamResp);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        HttpResponse<String> httpResponse = mockBareConnection.deleteIdentifier(SCN,
                                                                                ValidIdentifierSource.feide.asString(),
                                                                                "feide");

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = objectMapperWithEmpty.readValue(httpResponse.body(), Authority.class);
    }

    @Test
    public void testUpdateIdentifier() throws Exception {
        InputStream streamResp = UpdateAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);

        final String mockBody = IoUtils.streamToString(streamResp);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        HttpResponse<String> httpResponse = mockBareConnection.updateIdentifier(SCN,
                                                                                ValidIdentifierSource.feide.asString(),
                                                                                "feide", "updatedFeide");

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());
    }

    @Test
    public void testCreate() throws Exception {
        InputStream streamResp = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(
            BARE_SINGLE_AUTHORITY_CREATE_RESPONSE_JSON);
        final String mockBody = IoUtils.streamToString(streamResp);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        AuthorityConverter authorityConverter = new AuthorityConverter();
        BareAuthority bareAuthority = authorityConverter.buildAuthority(MOCK_NAME);
        HttpResponse<String> httpResponse = mockBareConnection.createAuthority(bareAuthority);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        BareAuthority createdAuthority = objectMapperWithEmpty.readValue(httpResponse.body(), BareAuthority.class);
        assertEquals(MOCK_NAME, authorityConverter.asAuthority(createdAuthority).getName());
    }

    @Test
    public void testGetMethodOnBareConnection() throws IOException, URISyntaxException, InterruptedException {
        InputStream fakeStream = AddNewAuthorityIdentifierHandlerTest.class
            .getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IoUtils.streamToString(fakeStream);

        BareConnection bareConnection = new BareConnection(mockHttpClient);
        when(mockHttpResponse.statusCode()).thenReturn(HTTP_OK);
        when(mockHttpResponse.body()).thenReturn(mockBody);

        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);
        final BareAuthority bareAuthority = bareConnection.get(SCN);
        assertNotNull(bareAuthority);
    }

    @Test
    public void testGetMethodOnBareConnection_ResponseFail() throws IOException,
                                                                    InterruptedException {
        BareConnection bareConnection = new BareConnection(mockHttpClient);

        when(mockHttpResponse.statusCode()).thenReturn(HTTP_NOT_ACCEPTABLE);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);
        assertThrows(IOException.class, () -> bareConnection.get(SCN));
    }
}
