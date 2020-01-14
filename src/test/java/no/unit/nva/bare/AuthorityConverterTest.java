package no.unit.nva.bare;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorityConverterTest {

    public static final String TEST_KEY_1 = "testKey1";
    public static final String TEST_VALUE_1 = "testValue1";
    public static final String TEST_KEY_2 = "testKey2";
    public static final String TEST_VALUE_2 = "testValue2";
    public static final String WRONG_KEY = "wrongKey";
    public static final String EMPTY_STRING = "";

    @Test
    public void testGetValueFromJson() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String testArray = String.format("{\"%s\": [\"%s\"],\"%s\": [\"%s\"]}", TEST_KEY_1, TEST_VALUE_1, TEST_KEY_2,
                TEST_VALUE_2);
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityConverter.getValueFromJsonArray(jsonObject, TEST_KEY_1);
        assertEquals(TEST_VALUE_1, value);
    }

    @Test
    public void testGetValueFromJsonWrongKey() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String testArray = String.format("{\"%s\": [\"%s\"],\"%s\": [\"%s\"]}", TEST_KEY_1, TEST_VALUE_1, TEST_KEY_2,
                TEST_VALUE_2);
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityConverter.getValueFromJsonArray(jsonObject, WRONG_KEY);
        assertEquals(EMPTY_STRING, value);
    }

    @Test
    public void testGetValueFromJsonEmptyArray() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String testArray = String.format("{\"%s\": \"%s\",\"%s\": [\"%s\"]}", TEST_KEY_1, TEST_VALUE_1,
                TEST_KEY_2, TEST_VALUE_2);
        final JsonObject jsonObject = (JsonObject) JsonParser.parseString(testArray);
        String value = authorityConverter.getValueFromJsonArray(jsonObject, TEST_KEY_1);
        assertEquals(EMPTY_STRING, value);
    }
}
