package no.unit.nva.bare;


import java.util.Arrays;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

/**
    This class resembles the pseudo-marc structure used in Bare.
*/
@JacocoGenerated
public class Marc21 {
    private String tag;
    private String ind1;
    private String ind2;
    private Subfield[] subfields;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Marc21)) {
            return false;
        }
        Marc21 marc21 = (Marc21) o;
        return Objects.equals(getTag(), marc21.getTag())
            && Objects.equals(getInd1(), marc21.getInd1())
            && Objects.equals(getInd2(), marc21.getInd2())
            && Arrays.equals(getSubfields(), marc21.getSubfields());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getTag(), getInd1(), getInd2());
        result = 31 * result + Arrays.hashCode(getSubfields());
        return result;
    }

    @Override
    public String toString() {
        return "Marc21{"
              + "tag='" + getTag() + '\''
              + ", ind1='" + getInd1() + '\''
              + ", ind2='" + getInd2() + '\''
              + ", subfields=" + Arrays.toString(getSubfields())
              + '}';
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getInd1() {
        return ind1;
    }

    public void setInd1(String ind1) {
        this.ind1 = ind1;
    }

    public String getInd2() {
        return ind2;
    }

    public void setInd2(String ind2) {
        this.ind2 = ind2;
    }

    public Subfield[] getSubfields() {
        return subfields.clone();
    }

    public void setSubfields(Subfield...subfields) {
        this.subfields = subfields.clone();
    }
}
