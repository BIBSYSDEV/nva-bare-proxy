package no.unit.nva.bare;

public abstract class AuthorityHandler {

    protected final GatewayResponse gatewayResponse;
    protected final transient BareConnection bareConnection;
    protected final transient AuthorityConverter authorityConverter = new AuthorityConverter();


    public AuthorityHandler() {
        this.gatewayResponse = new GatewayResponse();
        this.bareConnection = new BareConnection();
    }

    public AuthorityHandler(BareConnection bareConnection) {
        this.gatewayResponse = new GatewayResponse();
        this.bareConnection = bareConnection;
    }
}
