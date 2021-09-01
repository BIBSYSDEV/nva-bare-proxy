package no.unit.nva.bare;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.MISSING_ATTRIBUTE_IDENTIFIER;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_QUALIFIER;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.MISSING_REQUEST_JSON_BODY;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.QUALIFIER_KEY;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.REMOTE_SERVER_ERRORMESSAGE;
import static no.unit.nva.bare.AddNewAuthorityIdentifierHandler.SCN_KEY;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.testutils.IoUtils;
import no.unit.nva.testutils.TestHeaders;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class AddNewAuthorityIdentifierHandlerTest {

    public static final String MOCK_SCN_VALUE = "scn";
    public static final String MOCK_FEIDEID_VALUE = "feideid";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON = "bareSingleAuthorityGetResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";

    private Context context;
    private BareConnection bareConnection;
    private ByteArrayOutputStream output;
    private AddNewAuthorityIdentifierHandler addNewAuthorityIdentifierHandler;
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

        InputStream input = new HandlerRequestBuilder<AddNewAuthorityIdentifierRequest>(objectMapperWithEmpty)
            .build();
        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Assertions.assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_SCN));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsMissing() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, null);
        InputStream input = createRequest(pathParams, null);

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Assertions.assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Invalid")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsInvalid() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE,
                                                           ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        InputStream input = createRequest(pathParams, null);
        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Assertions.assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = createRequest(pathParams, null);

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Assertions.assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Attribute Value In Body Json")
    public void handlerReturnsBadRequestWhenMissingAttributeValueInBodyJson() throws IOException {

        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(null);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerRequestBuilder<AddNewAuthorityIdentifierRequest>(objectMapperWithEmpty)
            .withBody(requestObject)
            .withPathParameters(pathParams)
            .withHeaders(TestHeaders.getRequestHeaders())
            .build();

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Assertions.assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_ATTRIBUTE_IDENTIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Ok Response When Input Is Valid And Authority Identifier Is Added Successfully")
    public void handlerReturnsOkWhenInputIsValidAndAuthorityIdentifierIsAddedSuccessfully() throws Exception {

        InputStream is = IoUtils.inputStreamFromResources(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = objectMapperWithEmpty.readValue(new InputStreamReader(is),
                                                                            BareAuthority.class);
        when(bareConnection.get(anyString())).thenReturn(bareAuthority);
        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.addNewIdentifier(any(), any())).thenReturn(httpResponse);

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = createRequest(pathParams, requestObject);

        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);
        GatewayResponse<Authority> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Bare Connection Error")
    public void handlerReturnsInternalServerErrorWhenBareConnectionError() throws Exception {

        when(bareConnection.addNewIdentifier(any(), any())).thenThrow(
            new IOException(EXCEPTION_IS_EXPECTED));

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = createRequest(pathParams, requestObject);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Failing To Read Authority From Stream")
    public void handleReturnsInternalServerErrorWhenFailingToReadAuthorityFromStream() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.get(any())).thenReturn(null);
        when(bareConnection.addNewIdentifier(any(), any())).thenReturn(httpResponse);

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = createRequest(pathParams, requestObject);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Exception Getting Authority From Bare")
    public void handlerReturnsInternalServerErrorWhenExceptionGettingAuthorityFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_OK);
        when(bareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        when(bareConnection.addNewIdentifier(any(), any())).thenReturn(httpResponse);

        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = createRequest(pathParams, requestObject);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Unexpected Response From Bare")
    public void handlerReturnsInternalServerErrorWhenUnexpectedResponseFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(HTTP_FORBIDDEN);
        when(bareConnection.addNewIdentifier(any(), any())).thenReturn(httpResponse);
        addNewAuthorityIdentifierHandler = new AddNewAuthorityIdentifierHandler(bareConnection);
        AddNewAuthorityIdentifierRequest requestObject = new AddNewAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = createRequest(pathParams, requestObject);
        addNewAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse<Problem> gatewayResponse =
            nva.commons.apigateway.GatewayResponse.fromOutputStream(output);
        Problem problem = gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(REMOTE_SERVER_ERRORMESSAGE));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    private <T> InputStream createRequest(Map<String, String> pathParams, T requestBody)
        throws com.fasterxml.jackson.core.JsonProcessingException {
        return new HandlerRequestBuilder<T>(objectMapperWithEmpty)
            .withBody(requestBody)
            .withHeaders(TestHeaders.getRequestHeaders())
            .withPathParameters(pathParams)
            .build();
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
