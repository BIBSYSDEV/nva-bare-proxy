package no.unit.nva.bare;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.testutils.HandlerUtils;
import no.unit.nva.testutils.TestContext;
import no.unit.nva.testutils.TestHeaders;
import nva.commons.utils.Environment;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.*;
import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteAuthorityIdentifierHandlerTest {

    public static final String EMPTY_STRING = "";
    public static final String MOCK_SCN_VALUE = "scn";
    public static final String MOCK_ORCID_VALUE = "orgunitid";
    public static final String MOCK_FEIDEID_VALUE = "feideid";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON = "/bareSingleAuthorityGetResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String BODY_KEY = "body";
    public static final String MOCK_BODY = "postRequestBody";
    private Environment environment;
    private Context context;
    private BareConnection bareConnection;
    private OutputStream output;
    private DeleteAuthorityIdentifierHandler deleteAuthorityIdentifierHandler;


    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        context = new TestContext();

        output = new ByteArrayOutputStream();

        bareConnection = mock(BareConnection.class);
    }



    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When SCN Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenScnPathParameterIsMissing() throws IOException {

        InputStream input = new HandlerUtils(objectMapper).requestObjectToApiGatewayRequestInputSteam(null);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(environment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.handlers.GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), nva.commons.handlers.GatewayResponse.class);
        Assertions.assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);


        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_SCN));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsMissing() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, null);
        InputStream input = new HandlerUtils(objectMapper).requestObjectToApiGatewayRequestInputSteam(null, TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(environment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.handlers.GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), nva.commons.handlers.GatewayResponse.class);
        Assertions.assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Invalid")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsInvalid() throws IOException {


        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        InputStream input = new HandlerUtils(objectMapper).requestObjectToApiGatewayRequestInputSteam(null, TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(environment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.handlers.GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), nva.commons.handlers.GatewayResponse.class);
        Assertions.assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);


        assertThat(problem.getDetail(), containsString(INVALID_VALUE_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(objectMapper).requestObjectToApiGatewayRequestInputSteam(null, TestHeaders.getRequestHeaders(), pathParams, null);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(environment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.handlers.GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), nva.commons.handlers.GatewayResponse.class);
        Assertions.assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);


        assertThat(problem.getDetail(), containsString(MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Attribute Value In Body Json")
    public void handlerReturnsBadRequestWhenMissingAttributeValueInBodyJson() throws IOException {

        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(null);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(objectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject, TestHeaders.getRequestHeaders(), pathParams, null);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(environment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.handlers.GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), nva.commons.handlers.GatewayResponse.class);
        Assertions.assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);


        assertThat(problem.getDetail(), containsString(MISSING_ATTRIBUTE_IDENTIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    private Map<String, String> getPathParameters(String scn, String qualifier) {
        Map<String, String> pathParams = new ConcurrentHashMap<>();
        if(!org.apache.commons.lang3.StringUtils.isEmpty(scn)) {
            pathParams.put(SCN_KEY, scn);
        }
        if(!StringUtils.isEmpty(qualifier)) {
            pathParams.put(QUALIFIER_KEY, qualifier);
        }
        return pathParams;
    }
//
//    @Test
//    public void testDeleteAuthorityIdentifierFeideId() throws Exception {
//        HashMap<String, String> pathParams = new HashMap<>();
//        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
//        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.FEIDEID.asString());
//        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_FEIDEID_VALUE);
//        Map<String, Object> requestEvent = new HashMap<>();
//        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
//
//        InputStream stream1 =
//                DeleteAuthorityIdentifierHandlerTest.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
//        final BareAuthority bareAuthority = new Gson().fromJson(new InputStreamReader(stream1), BareAuthority.class);
//        when(mockBareConnection.get(anyString())).thenReturn(bareAuthority);
//
//        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
//        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenReturn(mockHttpResponse);
//        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
//                new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        GatewayResponse response = mockDeleteAuthorityHandler.handleRequest(requestEvent, null);
//        Authority responseAuthority = new Gson().fromJson(response.getBody(), Authority.class);
//        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
//    }
//
//    @Test
//    public void testResponseFromBareWhereStatusCodeBadRequest() throws IOException, URISyntaxException,
//            InterruptedException {
//        DeleteAuthorityIdentifierHandler handler = new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
//        when(mockBareConnection.deleteIdentifier(any(), any(), any()))
//                .thenReturn(mockHttpResponse);
//        final GatewayResponse gatewayResponse = handler.deleteIdentifier(MOCK_SCN_VALUE, "invalid",
//                MOCK_FEIDEID_VALUE);
//        assertEquals(DeleteAuthorityIdentifierHandler.ERROR_CALLING_REMOTE_SERVER, gatewayResponse.getStatusCode());
//    }
//
//    @Test
//    public void testDeleteAuthorityBareConnectionError() throws Exception {
//        HashMap<String, String> pathParams = new HashMap<>();
//        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
//        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
//        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
//        Map<String, Object> requestEvent = new HashMap<>();
//        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
//        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenThrow(
//                new IOException(EXCEPTION_IS_EXPECTED));
//        DeleteAuthorityIdentifierHandler mockUpdateAuthorityHandler =
//                new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        GatewayResponse response = mockUpdateAuthorityHandler.handleRequest(requestEvent, null);
//        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
//        String content = response.getBody();
//        assertNotNull(content);
//        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
//    }
//
//    @Test
//    public void testDeleteAuthority_failingToReadAuthorityFromStream() throws Exception {
//        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
//        when(mockBareConnection.get(any())).thenReturn(null);
//        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
//                new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        when(mockBareConnection.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
//                "may-britt.moser@ntnu.no"))
//                .thenReturn(mockHttpResponse);
//        GatewayResponse response =
//                mockDeleteAuthorityHandler.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
//                        "may-britt.moser@ntnu.no");
//        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
//        String content = response.getBody();
//        assertNotNull(content);
//    }
//
//    @Test
//    public void testDeleteAuthority_exceptionOnReadAuthorityFromBare() throws Exception {
//        when(mockHttpResponse.statusCode()).thenReturn(Response.Status.OK.getStatusCode());
//
//        when(mockBareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
//        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
//                new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        when(mockBareConnection.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
//                "may-britt.moser@ntnu.no"))
//                .thenReturn(mockHttpResponse);
//        GatewayResponse response =
//                mockDeleteAuthorityHandler.deleteIdentifier(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString(),
//                        "may-britt.moser@ntnu.no");
//        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
//        String content = response.getBody();
//        assertNotNull(content);
//        assertTrue(content.contains(EXCEPTION_IS_EXPECTED));
//    }
//
//    @Test
//    public void testDeleteAuthorityCommunicationErrors() throws IOException, URISyntaxException, InterruptedException {
//        HashMap<String, String> pathParams = new HashMap<>();
//        pathParams.put(DeleteAuthorityIdentifierHandler.SCN_KEY, MOCK_SCN_VALUE);
//        pathParams.put(DeleteAuthorityIdentifierHandler.QUALIFIER_KEY, ValidIdentifierKey.ORCID.asString());
//        pathParams.put(DeleteAuthorityIdentifierHandler.IDENTIFIER_KEY, MOCK_ORCID_VALUE);
//        Map<String, Object> requestEvent = new HashMap<>();
//        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);
//        DeleteAuthorityIdentifierHandler mockDeleteAuthorityHandler =
//                new DeleteAuthorityIdentifierHandler(mockBareConnection);
//        when(mockBareConnection.deleteIdentifier(any(), any(), any())).thenThrow(
//                new IOException(EXCEPTION_IS_EXPECTED));
//        GatewayResponse response = mockDeleteAuthorityHandler.handleRequest(requestEvent, null);
//        GatewayResponse expectedResponse = new GatewayResponse();
//        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//        expectedResponse.setErrorBody(EXCEPTION_IS_EXPECTED);
//        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
//        assertEquals(expectedResponse.getBody(), response.getBody());
//    }
}
