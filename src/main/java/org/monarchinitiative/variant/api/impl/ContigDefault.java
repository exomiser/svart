package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Contig;
import org.monarchinitiative.variant.api.SequenceRole;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class ContigDefault implements Contig {

    private final int id;
    private final String name;
    private final SequenceRole sequenceRole;
    private final int length;
    private final String genbankAccession;
    private final String refSeqAccession;
    private final String ucscName;

    private ContigDefault(int id, String name, SequenceRole sequenceRole, int length, String genbankAccession, String refSeqAccession, String ucscName) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.sequenceRole = sequenceRole;
        this.length = length;
        this.genbankAccession = Objects.requireNonNull(genbankAccession);
        this.refSeqAccession = Objects.requireNonNull(refSeqAccession);
        this.ucscName = Objects.requireNonNull(ucscName);
    }

    public static ContigDefault of(int id, String name, SequenceRole sequenceRole, int length, String genbankAccession, String refSeqAccession, String ucscName) {
        return new ContigDefault(id, name, sequenceRole, length, genbankAccession, refSeqAccession, ucscName);
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
        return sequenceRole;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String genBankAccession() {
        return genbankAccession;
    }

    @Override
    public String refSeqAccession() {
        return refSeqAccession;
    }

    @Override
    public String ucscName() {
        return ucscName;
    }

    @Override
    public int compareTo(Contig o) {
        return Integer.compare(this.id(), o.id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContigDefault)) return false;
        ContigDefault that = (ContigDefault) o;
        return id == that.id &&
                length == that.length &&
                name.equals(that.name) &&
                sequenceRole == that.sequenceRole &&
                genbankAccession.equals(that.genbankAccession) &&
                refSeqAccession.equals(that.refSeqAccession) &&
                ucscName.equals(that.ucscName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sequenceRole, length, genbankAccession, refSeqAccession, ucscName);
    }

    @Override
    public String toString() {
        return "ContigDefault{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sequenceRole=" + sequenceRole +
                ", length=" + length +
                ", genbankAccession='" + genbankAccession + '\'' +
                ", refSeqAccession='" + refSeqAccession + '\'' +
                ", ucscName='" + ucscName + '\'' +
                '}';
    }
}
