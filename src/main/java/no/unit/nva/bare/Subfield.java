package no.unit.nva.bare;

import nva.commons.utils.JacocoGenerated;

import java.util.Objects;

@JacocoGenerated
public class Subfield {
    private String subcode;
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subfield)) {
            return false;
        }
        Subfield subfield = (Subfield) o;
        return Objects.equals(getSubcode(), subfield.getSubcode())
            && Objects.equals(getValue(), subfield.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubcode(), getValue());
    }

    @Override
    public String toString() {
        return "Subfield{"
              + "subcode='" + getSubcode() + '\''
              + ", value='" + getValue() + '\''
              + '}';
    }

    public String getSubcode() {
        return subcode;
    }

    public void setSubcode(String subcode) {
        this.subcode = subcode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
