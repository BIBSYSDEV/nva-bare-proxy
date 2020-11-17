package no.unit.nva.bare;

import nva.commons.utils.JacocoGenerated;

import java.util.Arrays;
import java.util.Objects;

@JacocoGenerated
@SuppressWarnings("PMD")
public class Marc21 {
    public String tag;
    public String ind1;
    public String ind2;
    public Subfield[] subfields;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Marc21)) {
            return false;
        }
        Marc21 marc21 = (Marc21) o;
        return Objects.equals(tag, marc21.tag)
            && Objects.equals(ind1, marc21.ind1)
            && Objects.equals(ind2, marc21.ind2)
            && Arrays.equals(subfields, marc21.subfields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tag, ind1, ind2);
        result = 31 * result + Arrays.hashCode(subfields);
        return result;
    }

    @Override
    public String toString() {
        return "Marc21{" +
                "tag='" + tag + '\'' +
                ", ind1='" + ind1 + '\'' +
                ", ind2='" + ind2 + '\'' +
                ", subfields=" + Arrays.toString(subfields) +
                '}';
    }
}
