package no.unit.nva.bare.authorizer;

import no.unit.commons.apigateway.authentication.DefaultRequestAuthorizer;
import nva.commons.core.JacocoGenerated;

public class LambdaAuthorizer extends DefaultRequestAuthorizer {

    public static final String DEFAULT_PRINCIPAL_ID = "ServiceAccessingBareProxy";

    @JacocoGenerated
    public LambdaAuthorizer() {
        super(DEFAULT_PRINCIPAL_ID);
    }
}