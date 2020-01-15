package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
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
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BareConnectionTest {

    public static final String COMPLETE_SINGLE_AUTHORITY_JSON = "/completeSingleAuthority.json";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityGetResponseWithAllIds.json";
    public static final String NONSENSE_URL = "http://iam.an.url";
    public static final String SCN = "scn";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    CloseableHttpClient mockHttpClient;
    @Mock
    CloseableHttpResponse mockCloseableHttpResponse;
    @Mock
    HttpEntity mockEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test(expected = IOException.class)
    public void testExceptionOnBareConnection() throws IOException {
        BareConnection bareConnection = new BareConnection();
        URL emptyUrl = new URL(NONSENSE_URL);
        bareConnection.connect(emptyUrl);
        fail();
    }

    @Test
    public void testConnect() throws IOException {
        URL invalidUrl = Paths.get("/dev/null").toUri().toURL();
        BareConnection bareConnection = new BareConnection();
        final InputStreamReader connect = bareConnection.connect(invalidUrl);
        assertNotNull(connect);
    }

    @Test
    public void testUpdate() throws Exception {
        InputStream streamResp = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(
                BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(streamResp);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier(BareConnection.FEIDE, "feide");
        CloseableHttpResponse httpResponse = mockBareConnection.addIdentifier(SCN, authorityIdentifier);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.getEntity());

        InputStream inputStream = httpResponse.getEntity().getContent();
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority updatedAuthority = authorityConverter.extractAuthorityFrom(new InputStreamReader(inputStream));

        InputStream stream =
                AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IOUtils.toString(stream, Charset.defaultCharset());
        Type authorityListType = new TypeToken<ArrayList<Authority>>() {
        }.getType();
        List<Authority> mockAuthorityList = new Gson().fromJson(st, authorityListType);
        assertEquals(mockAuthorityList.get(0).getScn(), updatedAuthority.getScn());
        assertNotNull(updatedAuthority.getFeideIds());
        assertNotNull(updatedAuthority.getOrcIds());
    }

    @Test
    public void testGenerateGetUri() throws URISyntaxException {
        final URI uri = new BareConnection().generateGetUrl(SCN);
        assertNotNull(uri);

    }

    @Test
    public void testGenerateQueryUrl() throws IOException, URISyntaxException {
        final URL url = new BareConnection().generateQueryUrl("henrik ibsen");
        assertNotNull(url);

    }

    @Test
    public void testGetMethodOnBareConnection() throws IOException, URISyntaxException {
        BareConnection bareConnection = new BareConnection(mockHttpClient);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);

        String fakeInput = BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON;
        InputStream fakeStream =
                AddAuthorityIdentifierHandlerTest.class
                        .getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_WITH_ALL_IDS_JSON);
        when(mockEntity.getContent()).thenReturn(fakeStream);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);
        final BareAuthority bareAuthority = bareConnection.get(SCN);
        assertNotNull(bareAuthority);
    }

    @Test(expected = IOException.class)
    public void testGetMethodOnBareConnection_ResponseFail() throws IOException, URISyntaxException {
        BareConnection bareConnection = new BareConnection(mockHttpClient);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.NOT_ACCEPTABLE.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);
        final BareAuthority inputStreamReader = bareConnection.get(SCN);
        fail("where is my Exception?");
    }


}
