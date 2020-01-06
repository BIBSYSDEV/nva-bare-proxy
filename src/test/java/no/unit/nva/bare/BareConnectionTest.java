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
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class BareConnectionTest {

    public static final String COMPLETE_SINGLE_AUTHORITY_JSON = "/completeSingleAuthority.json";
    public static final String BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON = "/bareSingleAuthorityResponseWithAllIds.json";

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
        URL emptUrl = new URL("http://iam.an.url");
        bareConnection.connect(emptUrl);
        Assert.fail();
    }

    @Test
    public void testUpdate() throws Exception {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        InputStream stream = UpdateAuthorityHandlerTest.class.getResourceAsStream(COMPLETE_SINGLE_AUTHORITY_JSON);
        String st = IOUtils.toString(stream, Charset.defaultCharset());
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> mockAuthorityList = new Gson().fromJson(st, authorityListType);
        BareConnection mockBareConnection = new BareConnection(mockHttpClient);
        InputStream streamResp = UpdateAuthorityHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_RESPONSE_WITH_ALL_IDS_JSON);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(streamResp);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);
        CloseableHttpResponse httpResponse = mockBareConnection.update(mockAuthorityList.get(0));
        assertNotNull(httpResponse);
        assertNotNull(httpResponse.getEntity());
        InputStream inputStream = httpResponse.getEntity().getContent();
        String content = IOUtils.toString(inputStream, Charset.defaultCharset());
        List<Authority> updatedAuthority = authorityConverter.extractAuthoritiesFrom(content);
        assertEquals(1, updatedAuthority.size());
        assertEquals(mockAuthorityList.get(0).getScn(), updatedAuthority.get(0).getScn());
    }

}