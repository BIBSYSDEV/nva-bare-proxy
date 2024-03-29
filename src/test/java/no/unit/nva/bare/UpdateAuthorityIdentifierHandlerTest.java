package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.bare.ApplicationConfig.defaultRestObjectMapper;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.INVALID_VALUE_PATH_PARAMETER_QUALIFIER;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.MISSING_ATTRIBUTE_IDENTIFIER;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.MISSING_ATTRIBUTE_UPDATED_IDENTIFIER;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_QUALIFIER;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.MISSING_REQUEST_JSON_BODY;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.QUALIFIER_KEY;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.REMOTE_SERVER_ERRORMESSAGE;
import static no.unit.nva.bare.UpdateAuthorityIdentifierHandler.SCN_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.testutils.HandlerUtils;
import no.unit.nva.testutils.TestHeaders;
import nva.commons.core.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class UpdateAuthorityIdentifierHandlerTest {

    public static final String MOCK_SCN_VALUE = "scn";
    public static final String MOCK_FEIDEID_VALUE = "feideid";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON = "/bareSingleAuthorityGetResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";

    public static final URI MOCK_IDENTIFIER_URI = URI.create("https://example.org/originalidentifier");
    public static final URI MOCK_UPDATED_IDENTIFIER_URI = URI.create("https://example.org/originalidentifier");

    private Context context;
    private BareConnection bareConnection;
    private OutputStream output;
    private UpdateAuthorityIdentifierHandler updateAuthorityIdentifierHandler;
    private HttpResponse httpResponse;

    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        bareConnection = mock(BareConnection.class);
        httpResponse = mock(HttpResponse.class);
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When SCN Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenScnPathParameterIsMissing() throws IOException {

        InputStream input = new HandlerUtils(defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_SCN));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsMissing() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, null);
        InputStream input = new HandlerUtils(defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                               TestHeaders.getRequestHeaders(),
                                                                                                               pathParams,
                                                                                                               null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = defaultRestObjectMapper.readValue(output.toString(),
                                                                                                 nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Invalid")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsInvalid() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE,
                                                           ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        InputStream input = new HandlerUtils(defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                               TestHeaders.getRequestHeaders(),
                                                                                                               pathParams,
                                                                                                               null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(INVALID_VALUE_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                               TestHeaders.getRequestHeaders(),
                                                                                                               pathParams,
                                                                                                               null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Identifier Attribute Value In Body Json")
    public void handlerReturnsBadRequestWhenMissingIdentifierAttributeValueInBodyJson() throws IOException {

        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(null,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_ATTRIBUTE_IDENTIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Updated Identifier Attribute Value In Body Json")
    public void handlerReturnsBadRequestWhenMissingUpdatedIdentifierAttributeValueInBodyJson() throws IOException {

        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              null);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_ATTRIBUTE_UPDATED_IDENTIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Ok Response When Input Is Valid And Authority Identifier Is Updated Successfully")
    public void handlerReturnsOkWhenInputIsValidAndAuthorityIdentifierIsUpdatedSuccessfully() throws Exception {

        InputStream is =
            UpdateAuthorityIdentifierHandler.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = defaultRestObjectMapper.readValue(new InputStreamReader(is),
                                                                            BareAuthority.class);
        when(bareConnection.get(anyString())).thenReturn(bareAuthority);
        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.updateIdentifier(any(), any(), any(), any())).thenReturn(httpResponse);

        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);
        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(),
                                            nva.commons.apigateway.GatewayResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    @DisplayName("handler Returns Ok Response When Input Is Valid URI And Authority Identifier Is Updated Successfully")
    public void handlerReturnsOkWhenInputIsValidUriAndAuthorityIdentifierIsUpdatedSuccessfully() throws Exception {

        initMockBareConnection();
        initMockUpdateAuthorityIdentifierHandler();

        UpdateAuthorityIdentifierRequest requestObject =
            new UpdateAuthorityIdentifierRequest(MOCK_IDENTIFIER_URI.toString(),
                                                 MOCK_UPDATED_IDENTIFIER_URI.toString());
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);
        nva.commons.apigateway.GatewayResponse gatewayResponse =
            defaultRestObjectMapper.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Bare Connection Error")
    public void handlerReturnsInternalServerErrorWhenBareConnectionError() throws Exception {

        when(bareConnection.updateIdentifier(any(), any(), any(), any())).thenThrow(
            new IOException(EXCEPTION_IS_EXPECTED));

        UpdateAuthorityIdentifierHandler updateAuthorityIdentifierHandler =
            new UpdateAuthorityIdentifierHandler(bareConnection);
        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = defaultRestObjectMapper.readValue(output.toString(),
                                                                                                 nva.commons.apigateway.GatewayResponse.class);
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Failing To Read Authority From Stream")
    public void handleReturnsInternalServerErrorWhenFailingToReadAuthorityFromStream() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.get(any())).thenReturn(null);
        when(bareConnection.updateIdentifier(any(), any(), any(), any())).thenReturn(httpResponse);

        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = defaultRestObjectMapper.readValue(output.toString(),
                                                                                                 nva.commons.apigateway.GatewayResponse.class);
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Exception Getting Authority From Bare")
    public void handlerReturnsInternalServerErrorWhenExceptionGettingAuthorityFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        when(bareConnection.updateIdentifier(any(), any(), any(), any())).thenReturn(httpResponse);

        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = defaultRestObjectMapper.readValue(output.toString(),
                                                                                                 nva.commons.apigateway.GatewayResponse.class);
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Unexpected Response From Bare")
    public void handlerReturnsInternalServerErrorWhenUnexpectedResponseFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_FORBIDDEN);
        when(bareConnection.updateIdentifier(any(), any(), any(), any())).thenReturn(httpResponse);

        updateAuthorityIdentifierHandler = new UpdateAuthorityIdentifierHandler(bareConnection);
        UpdateAuthorityIdentifierRequest requestObject = new UpdateAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE,
                                                                                              MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            defaultRestObjectMapper).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                              TestHeaders.getRequestHeaders(),
                                                                              pathParams, null);
        updateAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = defaultRestObjectMapper.readValue(output.toString(),
                                                                                                 nva.commons.apigateway.GatewayResponse.class);
        Problem problem = defaultRestObjectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(REMOTE_SERVER_ERRORMESSAGE));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    private void initMockUpdateAuthorityIdentifierHandler() throws
                                                            InterruptedException, BareCommunicationException,
                                                            BareException {
        updateAuthorityIdentifierHandler = spy(new UpdateAuthorityIdentifierHandler(bareConnection));
        Authority mockAuthority = mock(Authority.class);
        doReturn(mockAuthority).when(updateAuthorityIdentifierHandler).getAuthority(any());
    }

    private void initMockBareConnection() throws IOException, InterruptedException {

        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.statusCode()).thenReturn(HTTP_OK);
        when(httpClient.send(any(), any())).thenReturn(mockHttpResponse);
        bareConnection = new BareConnection(httpClient);
    }

    private Map<String, String> getPathParameters(String scn, String qualifier) {
        Map<String, String> pathParams = new ConcurrentHashMap<>();
        if (StringUtils.isNotEmpty(scn)) {
            pathParams.put(SCN_KEY, scn);
        }
        if (StringUtils.isNotEmpty(qualifier)) {
            pathParams.put(QUALIFIER_KEY, qualifier);
        }
        return pathParams;
    }
}