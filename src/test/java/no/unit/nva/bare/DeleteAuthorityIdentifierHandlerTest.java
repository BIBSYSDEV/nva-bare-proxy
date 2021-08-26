package no.unit.nva.bare;

import static no.unit.nva.bare.AuthorityConverterTest.HTTPS_LOCALHOST_PERSON;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.INVALID_VALUE_PATH_PARAMETER_QUALIFIER;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.MISSING_ATTRIBUTE_IDENTIFIER;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_QUALIFIER;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.MISSING_PATH_PARAMETER_SCN;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.MISSING_REQUEST_JSON_BODY;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.QUALIFIER_KEY;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.REMOTE_SERVER_ERRORMESSAGE;
import static no.unit.nva.bare.DeleteAuthorityIdentifierHandler.SCN_KEY;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.JsonUtils.objectMapperWithEmpty;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
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
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.testutils.HandlerUtils;
import no.unit.nva.testutils.TestHeaders;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class DeleteAuthorityIdentifierHandlerTest {

    public static final String MOCK_SCN_VALUE = "scn";
    public static final String MOCK_FEIDEID_VALUE = "feideid";
    public static final String BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON = "/bareSingleAuthorityGetResponse.json";
    public static final String EXCEPTION_IS_EXPECTED = "Exception is expected.";

    private Environment mockEnvironment;
    private Context context;
    private BareConnection bareConnection;
    private OutputStream output;
    private DeleteAuthorityIdentifierHandler deleteAuthorityIdentifierHandler;
    private HttpResponse httpResponse;

    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(mockEnvironment.readEnv(AuthorityConverter.PERSON_AUTHORITY_BASE_ADDRESS_KEY))
            .thenReturn(HTTPS_LOCALHOST_PERSON);

        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        bareConnection = mock(BareConnection.class);
        httpResponse = mock(HttpResponse.class);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When SCN Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenScnPathParameterIsMissing() throws IOException {

        InputStream input = new HandlerUtils(objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(null);
        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_SCN));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Missing")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsMissing() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, null);
        InputStream input = new HandlerUtils(objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                      TestHeaders.getRequestHeaders(),
                                                                                                      pathParams, null);
        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Qualifier Path Parameter Is Invalid")
    public void handlerReturnsBadRequestWhenQualifierPathParameterIsInvalid() throws IOException {

        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE,
                                                           ValidIdentifierKey.ORGUNITID.asString() + "invalid");
        InputStream input = new HandlerUtils(objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                      TestHeaders.getRequestHeaders(),
                                                                                                      pathParams, null);
        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(INVALID_VALUE_PATH_PARAMETER_QUALIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(null,
                                                                                                      TestHeaders.getRequestHeaders(),
                                                                                                      pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Attribute Value In Body Json")
    public void handlerReturnsBadRequestWhenMissingAttributeValueInBodyJson() throws IOException {

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(null);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.ORGUNITID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(MISSING_ATTRIBUTE_IDENTIFIER));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Ok Response When Input Is Valid And Authority Identifier Is Deleted Successfully")
    public void handlerReturnsOkWhenInputIsValidAndAuthorityIdentifierIsDeletedSuccessfully() throws Exception {

        InputStream is =
            DeleteAuthorityIdentifierHandler.class.getResourceAsStream(BARE_SINGLE_AUTHORITY_GET_RESPONSE_JSON);
        final BareAuthority bareAuthority = objectMapperWithEmpty.readValue(new InputStreamReader(is), BareAuthority.class);

        when(bareConnection.get(anyString())).thenReturn(bareAuthority);
        when(httpResponse.statusCode()).thenReturn(SC_OK);
        when(bareConnection.deleteIdentifier(any(), any(), any())).thenReturn(httpResponse);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);
        nva.commons.apigateway.GatewayResponse gatewayResponse =
            objectMapperWithEmpty.readValue(output.toString(), nva.commons.apigateway.GatewayResponse.class);

        assertEquals(SC_OK, gatewayResponse.getStatusCode());
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Bare Connection Error")
    public void handlerReturnsInternalServerErrorWhenBareConnectionError() throws Exception {

        when(bareConnection.deleteIdentifier(any(), any(), any())).thenThrow(
            new IOException(EXCEPTION_IS_EXPECTED));

        deleteAuthorityIdentifierHandler =
            new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Failing To Read Authority From Stream")
    public void handleReturnsInternalServerErrorWhenFailingToReadAuthorityFromStream() throws Exception {

        when(httpResponse.statusCode()).thenReturn(SC_OK);
        when(bareConnection.get(any())).thenReturn(null);
        when(bareConnection.deleteIdentifier(any(), any(), any())).thenReturn(httpResponse);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(COMMUNICATION_ERROR_WHILE_RETRIEVING_UPDATED_AUTHORITY));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Exception Getting Authority From Bare")
    public void handlerReturnsInternalServerErrorWhenExceptionGettingAuthorityFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(SC_OK);
        when(bareConnection.get(any())).thenThrow(new IOException(EXCEPTION_IS_EXPECTED));
        when(bareConnection.deleteIdentifier(any(), any(), any())).thenReturn(httpResponse);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        nva.commons.apigateway.GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(),
                                                                                        nva.commons.apigateway.GatewayResponse.class);
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(EXCEPTION_IS_EXPECTED));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Unexpected Response From Bare")
    public void handlerReturnsInternalServerErrorWhenUnexpectedResponseFromBare() throws Exception {

        when(httpResponse.statusCode()).thenReturn(SC_FORBIDDEN);
        when(bareConnection.deleteIdentifier(any(), any(), any())).thenReturn(httpResponse);

        deleteAuthorityIdentifierHandler = new DeleteAuthorityIdentifierHandler(mockEnvironment, bareConnection);
        DeleteAuthorityIdentifierRequest requestObject = new DeleteAuthorityIdentifierRequest(MOCK_FEIDEID_VALUE);
        Map<String, String> pathParams = getPathParameters(MOCK_SCN_VALUE, ValidIdentifierKey.FEIDEID.asString());
        InputStream input = new HandlerUtils(
            objectMapperWithEmpty).requestObjectToApiGatewayRequestInputSteam(requestObject,
                                                                     TestHeaders.getRequestHeaders(), pathParams, null);
        deleteAuthorityIdentifierHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapperWithEmpty.readValue(output.toString(), GatewayResponse.class);
        Problem problem = objectMapperWithEmpty.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(REMOTE_SERVER_ERRORMESSAGE));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
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
