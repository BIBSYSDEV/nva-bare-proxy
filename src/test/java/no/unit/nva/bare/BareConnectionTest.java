package no.unit.nva.bare;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class BareConnectionTest {


    @Test(expected = IOException.class)
    public void testExceptionOnBareConnection() throws IOException {
        BareConnection bareConnection = new BareConnection();
        URL emptUrl = new URL("http://iam.an.url");
        bareConnection.connect(emptUrl);
        Assert.fail();
    }
}
