package no.unit.nva.bare;

import static java.util.Objects.isNull;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;

public class CreateAuthorityRequest {

    public static final String COMMA = ",";
    public static final String MALFORMED_NAME_VALUE = "The name value seems not to be in inverted form.";
    public static final String BODY_ARGS_MISSING = "Nothing to create. 'name' is missing.";

    @JsonProperty("invertedName")
    @JsonAlias("invertedname")
    private final String invertedName;

    @JsonCreator
    public CreateAuthorityRequest(@JsonProperty("invertedname") String invertedname) {
        this.invertedName = invertedname;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getInvertedName());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreateAuthorityRequest)) {
            return false;
        }
        CreateAuthorityRequest that = (CreateAuthorityRequest) o;
        return Objects.equals(getInvertedName(), that.getInvertedName());
    }

    @JacocoGenerated
    public String getInvertedName() {
        return invertedName;
    }

    public CreateAuthorityRequest validate() throws BadRequestException {
        if (isNull(invertedName)) {
            throw new BadRequestException(BODY_ARGS_MISSING);
        }
        if (!invertedName.contains(COMMA)) {
            throw new BadRequestException(MALFORMED_NAME_VALUE);
        }
        return this;
    }
}
