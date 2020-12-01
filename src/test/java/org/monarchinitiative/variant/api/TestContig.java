package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestContig implements Contig {

    private final int id;
    private final String name;
    private final int length;

    public TestContig(int id, String name, int length) {
        this.id = id;
        this.name = name;
        this.length = length;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return name;
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
        return "chr" + name;
    }

    @Override
    public int compareTo(Contig o) {
        return Integer.compare(id, o.id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestContig)) return false;
        TestContig that = (TestContig) o;
        return id == that.id &&
                length == that.length &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, length);
    }

    @Override
    public String toString() {
        return "TestContig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", length=" + length +
                '}';
    }
}
