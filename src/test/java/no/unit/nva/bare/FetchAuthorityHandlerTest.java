package no.unit.nva.bare;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchAuthorityHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    BareConnection mockBareConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccessfulResponseWithNameParam() throws IOException {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareSingleAuthorityResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"may-britt.moser@ntnu.no\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = "[\n"
                + "  {\n"
                + "    \"name\": \"Moser, May-Britt\",\n"
                + "    \"scn\": \"90517730\",\n"
                + "    \"feideId\": \"\",\n"
                + "    \"orcId\": \"\",\n"
                + "    \"birthDate\": \"1963-\"\n"
                + "  }\n"
                + "]";
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithFeideIdParam() throws IOException {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareSingleAuthorityResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"may-britt.moser@ntnu.no\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        String postResponseBody = "[\n"
                + "  {\n"
                + "    \"name\": \"Moser, May-Britt\",\n"
                + "    \"scn\": \"90517730\",\n"
                + "    \"feideId\": \"\",\n"
                + "    \"orcId\": \"\",\n"
                + "    \"birthDate\": \"1963-\"\n"
                + "  }\n"
                + "]";
        assertEquals(postResponseBody, content);
    }

    @Test
    public void testSuccessfulResponseWithOrcIdParam() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareSingleAuthorityResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        String postResponseBody = "[\n"
                + "  {\n"
                + "    \"name\": \"Moser, May-Britt\",\n"
                + "    \"scn\": \"90517730\",\n"
                + "    \"feideId\": \"\",\n"
                + "    \"orcId\": \"\",\n"
                + "    \"birthDate\": \"1963-\"\n"
                + "  }\n"
                + "]";
        List<Authority> expectedResponseAuthority = new Gson().fromJson(postResponseBody, authorityListType);
        assertEquals(expectedResponseAuthority.get(0).getScn(), responseAuthority.get(0).getScn());
        assertEquals(expectedResponseAuthority.get(0).getBirthDate(), responseAuthority.get(0).getBirthDate());
    }

    @Test
    public void testEmptyHitListResponse() throws Exception {
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        InputStream inputStream = FetchAuthorityHandlerTest.class.getResourceAsStream("/bareEmptyResponse.json");
        InputStreamReader bareResponseStreamReader = new InputStreamReader(inputStream);
        when(mockBareConnection.connect(any())).thenReturn(bareResponseStreamReader);
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        String postRequestBody = "{\n"
                + "\"name\": \"\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"0000-0001-7884-3049\"\n"
                + "}";
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.OK, result.getStatus());
        assertEquals(result.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        String content = result.getBody();
        assertNotNull(content);
        Type authorityListType = new TypeToken<ArrayList<Authority>>(){}.getType();
        List<Authority> responseAuthority = new Gson().fromJson(content, authorityListType);
        assertTrue("The result should be an empty list", responseAuthority.isEmpty());
    }

    @Test
    public void testFailingRequest() throws IOException {
        String postRequestBody = "{\n"
                + "\"name\": \"May-Britt Moser\",\n"
                + "\"feideId\": \"\",\n"
                + "\"orcId\": \"\"\n"
                + "}";
        FetchAuthorityHandler mockFetchAuthorityHandler = new FetchAuthorityHandler(mockBareConnection);
        String expectdExceptionMsg = "my mock throws an exception";
        when(mockBareConnection.connect(any())).thenThrow(new IOException(expectdExceptionMsg));
        when(mockBareConnection.generateQueryUrl(anyString())).thenCallRealMethod();
        GatewayResponse result = mockFetchAuthorityHandler.handleRequest(postRequestBody);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, result.getStatus());
        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains(expectdExceptionMsg));
    }

    @Test
    public void testSelectQueryParameter() {
        Authority authority = new Authority();
        authority.setName("");
        String feideId = "bob@unit.no";
        authority.setFeideId(feideId);
        authority.setOrcId("");
        FetchAuthorityHandler fetchAuthorityHandler = new FetchAuthorityHandler();
        String queryParameter = fetchAuthorityHandler.selectQueryParameter(authority);
        assertEquals(feideId, queryParameter);
    }

}
