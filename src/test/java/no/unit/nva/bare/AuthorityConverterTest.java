package no.unit.nva.bare;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorityConverterTest {

    @Test
    public void testGetValueFromJson() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String testKey1 = "testKey1";
        String testValue1 = "testValue1";
        String testKey2 = "testKey2";
        String testValue2 = "testValue2";
        String testArray = "{\"" + testKey1 + "\": [\"" + testValue1 + "\"],"
                + "\"" + testKey2 + "\": [\"" + testValue2 + "\"]}";
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityConverter.getValueFromJsonArray(jsonObject, testKey1);
        assertEquals(testValue1, value);
    }
}
