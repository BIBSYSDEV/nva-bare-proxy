package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BareConnectionTest {

    public static final String COMPLETE_SINGLE_AUTHORITY_JSON = "/completeSingleAuthority.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON =
            "/bareSingleAuthorityResponseWithAllIds.json";
    public static final String NONSENSE_URL = "http://iam.an.url";

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
        Assert.fail();
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
                BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(streamResp);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        InputStream stream = AddAuthorityIdentifierHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IOUtils.toString(stream, Charset.defaultCharset());
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> mockAuthorityList = new Gson().fromJson(st, authorityListType);
        BareConnection mockBareConnection = new BareConnection(mockHttpClient);

        String scn = "scn";
        AuthorityIdentifier authorityIdentifier = new AuthorityIdentifier("feide", "feide");
        CloseableHttpResponse httpResponse = mockBareConnection.addIdentifier(scn, authorityIdentifier);

        assertNotNull(httpResponse);
        assertNotNull(httpResponse.getEntity());

        InputStream inputStream = httpResponse.getEntity().getContent();
        String content = IOUtils.toString(inputStream, Charset.defaultCharset());
        AuthorityConverter authorityConverter = new AuthorityConverter();
        List<Authority> updatedAuthority = authorityConverter.extractAuthoritiesFrom(content);

        assertEquals(1, updatedAuthority.size());
        assertEquals(mockAuthorityList.get(0).getScn(), updatedAuthority.get(0).getScn());
    }

    @Test
    public void testGenerateGetUrl() throws IOException, URISyntaxException {
        final URL url = new BareConnection().generateGetUrl("scn");
        assertNotNull(url);

    }

    @Test
    public void testGenerateQueryUrl() throws IOException, URISyntaxException {
        final URL url = new BareConnection().generateQueryUrl("henrik ibsen");
        assertNotNull(url);

    }



}
