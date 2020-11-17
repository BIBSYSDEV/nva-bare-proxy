package no.unit.nva.bare;

import nva.commons.utils.JacocoGenerated;

import java.util.Objects;

@JacocoGenerated
@SuppressWarnings("PMD")
public class Subfield {
    public String subcode;
    public String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subfield)) {
            return false;
        }
        Subfield subfield = (Subfield) o;
        return Objects.equals(subcode, subfield.subcode) &&
                Objects.equals(value, subfield.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subcode, value);
    }

    @Override
    public String toString() {
        return "Subfield{" +
                "subcode='" + subcode + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
