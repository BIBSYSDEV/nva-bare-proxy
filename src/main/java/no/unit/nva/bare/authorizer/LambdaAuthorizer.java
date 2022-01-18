package no.unit.nva.bare.authorizer;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import no.unit.commons.apigateway.authentication.RequestAuthorizer;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

import static nva.commons.core.attempt.Try.attempt;

public class LambdaAuthorizer extends RequestAuthorizer {

    private static final Environment ENVIRONMENT = new Environment();

    public static final String DEFAULT_PRINCIPAL_ID = "ServiceAccessingBareProxy";
    public static final String AWS_SECRET_NAME = ENVIRONMENT.readEnv("API_SECRET_NAME");
    public static final String AWS_SECRET_KEY = ENVIRONMENT.readEnv("API_SECRET_KEY");
    private transient final AWSSecretsManager awsSecretsManager;

    @JacocoGenerated
    public LambdaAuthorizer() {
        this(newAwsSecretsManager());
    }

    public LambdaAuthorizer(AWSSecretsManager awsSecretsManager) {
        super(ENVIRONMENT);
        this.awsSecretsManager = awsSecretsManager;
    }

    @Override
    protected String principalId() {
        return DEFAULT_PRINCIPAL_ID;
    }

    @Override
    protected String fetchSecret() {
        SecretsReader secretsReader = new SecretsReader(awsSecretsManager);
        return attempt(() -> secretsReader.fetchSecret(AWS_SECRET_NAME, AWS_SECRET_KEY))
                .orElseThrow();
    }

    @JacocoGenerated
    private static AWSSecretsManager newAwsSecretsManager() {
        return AWSSecretsManagerClientBuilder.defaultClient();
    }
}