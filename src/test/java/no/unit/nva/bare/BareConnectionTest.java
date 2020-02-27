package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BareConnectionTest {

    public static final String COMPLETE_SINGLE_AUTHORITY_JSON = "/completeSingleAuthority.json";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String BARE_SINGLE_AUTHORITY_CREATE_RESPONSE_JSON = "/bareSingleAuthorityCreateResponse.json";
    public static final String NONSENSE_URL = "http://iam.an.url";
    public static final String SCN = "scn";
    public static final String MOCK_NAME = "Unit, DotNo";
    private static final String MOCK_BARE_HOST = "authority.bibsys.no";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    HttpClient mockHttpClient;
    @Mock
    HttpResponse mockHttpResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Config.getInstance().setBareHost(MOCK_BARE_HOST);
    }

    @Test
    public void testConnect() throws IOException {
        final URL localFileUrl = BareConnectionTest.class.getResource(BARE_SINGLE_AUTHORITY_CREATE_RESPONSE_JSON);
        BareConnection bareConnection = new BareConnection();
        final InputStreamReader streamReader = bareConnection.connect(localFileUrl);
        assertNotNull(streamReader);
        streamReader.close();
    }

    @Test(expected = IOException.class)
    public void testExceptionOnBareConnection() throws IOException {
        BareConnection bareConnection = new BareConnection();
        URL emptyUrl = new URL(NONSENSE_URL);
        bareConnection.connect(emptyUrl);
        fail();
    }

    @Test
    public void testUpdate() throws Exception {
        InputStream streamResp = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IOUtils.toString(streamResp, StandardCharsets.UTF_8);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        AuthorityIdentifier authorityIdentifier =
                new AuthorityIdentifier(ValidIdentifierSource.feide.asString(), "feide");
        HttpResponse<String> httpResponse = mockBareConnection.addIdentifier(SCN, authorityIdentifier);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = new Gson().fromJson(httpResponse.body(), Authority.class);

        InputStream stream =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IOUtils.toString(stream, Charset.defaultCharset());
        Type authorityListType = new TypeToken<ArrayList<Authority>>() {
        }.getType();
        List<Authority> mockAuthorityList = new Gson().fromJson(st, authorityListType);
        assertEquals(mockAuthorityList.get(0).getSystemControlNumber(), updatedAuthority.getSystemControlNumber());
        assertNotNull(updatedAuthority.getFeideids());
        assertNotNull(updatedAuthority.getOrcids());
        assertNotNull(updatedAuthority.getOrgunitids());
    }

    @Test
    public void testAddNewIdentifier() throws Exception {
        InputStream streamResp = AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IOUtils.toString(streamResp, StandardCharsets.UTF_8);
        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);
        HttpResponse<String> httpResponse = mockBareConnection.addNewIdentifier(SCN,
                ValidIdentifierKey.FEIDEID.asString(), "feide");

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = new Gson().fromJson(httpResponse.body(), Authority.class);

        InputStream stream =
                AddNewAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IOUtils.toString(stream, Charset.defaultCharset());
        Type authorityListType = new TypeToken<ArrayList<Authority>>() {
        }.getType();
        List<Authority> mockAuthorityList = new Gson().fromJson(st, authorityListType);
        assertEquals(mockAuthorityList.get(0).getSystemControlNumber(), updatedAuthority.getSystemControlNumber());
        assertNotNull(updatedAuthority.getFeideids());
        assertNotNull(updatedAuthority.getOrcids());
        assertNotNull(updatedAuthority.getOrgunitids());
    }

    @Test
    public void testDeleteIdentifier() throws Exception {
        InputStream streamResp = DeleteAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IOUtils.toString(streamResp, StandardCharsets.UTF_8);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        HttpResponse<String> httpResponse = mockBareConnection.deleteIdentifier(SCN,
                ValidIdentifierSource.feide.asString(), "feide");

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        Authority updatedAuthority = new Gson().fromJson(httpResponse.body(), Authority.class);

    }

    @Test
    public void testUpdateIdentifier() throws Exception {
        InputStream streamResp = UpdateAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);

        final String mockBody = IOUtils.toString(streamResp, StandardCharsets.UTF_8);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        HttpResponse<String> httpResponse = mockBareConnection.updateIdentifier(SCN,
                ValidIdentifierSource.feide.asString(), "feide", "updatedFeide");

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

    }

    @Test
    public void testCreate() throws Exception {
        InputStream streamResp = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_CREATE_RESPONSE_JSON);
        final String mockBody = IOUtils.toString(streamResp, StandardCharsets.UTF_8);

        when(mockHttpResponse.body()).thenReturn(mockBody);
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        AuthorityConverter authorityConverter = new AuthorityConverter();
        BareAuthority bareAuthority = authorityConverter.buildAuthority(MOCK_NAME);
        HttpResponse<String> httpResponse = mockBareConnection.createAuthority(bareAuthority);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());

        BareAuthority createdAuthority = new Gson().fromJson(httpResponse.body(), BareAuthority.class);
        assertEquals(MOCK_NAME, authorityConverter.asAuthority(createdAuthority).getName());
    }

    @Test
    public void testGenerateQueryUrl() throws IOException, URISyntaxException {
        final URL url = new BareConnection().generateQueryUrl("henrik ibsen");
        assertNotNull(url);
    }

    @Test
    public void testGetMethodOnBareConnection() throws IOException, URISyntaxException, InterruptedException {
        InputStream fakeStream = AddAuthorityIdentifierHandlerTest.class
                .getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        final String mockBody = IOUtils.toString(fakeStream, StandardCharsets.UTF_8);

        BareConnection bareConnection = new BareConnection(mockHttpClient);
        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockHttpResponse.body()).thenReturn(mockBody);

        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);
        final BareAuthority bareAuthority = bareConnection.get(SCN);
        assertNotNull(bareAuthority);
    }

    @Test(expected = IOException.class)
    public void testGetMethodOnBareConnection_ResponseFail() throws IOException, URISyntaxException,
            InterruptedException {
        BareConnection bareConnection = new BareConnection(mockHttpClient);

        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.NOT_ACCEPTABLE.getStatusCode());
        when(mockHttpClient.send(any(), any())).thenReturn(mockHttpResponse);
        final BareAuthority inputStreamReader = bareConnection.get(SCN);
        fail("where is my Exception?");
    }


}
