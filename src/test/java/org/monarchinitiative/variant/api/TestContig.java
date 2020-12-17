package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestContig implements Contig {

    private final int id;
    private final int length;

    public TestContig(int id, int length) {
        this.id = id;
        this.length = length;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return String.valueOf(id);
    }

    @Override
    public SequenceRole sequenceRole() {
        return SequenceRole.UNKNOWN;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String genBankAccession() {
        return "";
    }

    @Override
    public String refSeqAccession() {
        return "";
    }

    @Override
    public String ucscName() {
        return "chr" + name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestContig)) return false;
        TestContig that = (TestContig) o;
        return id == that.id &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, length);
    }

    @Override
    public String toString() {
        return "TestContig{" +
                "id=" + id +
                ", length=" + length +
                '}';
    }
}
