package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Contig;
import org.monarchinitiative.variant.api.SequenceRole;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultContig implements Contig {

    private final int id;
    private final String name;
    private final SequenceRole sequenceRole;
    private final int length;
    private final String genBankAccession;
    private final String refSeqAccession;
    private final String ucscName;

    private DefaultContig(int id, String name, SequenceRole sequenceRole, int length, String genBankAccession, String refSeqAccession, String ucscName) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.sequenceRole = sequenceRole;
        this.length = length;
        this.genBankAccession = Objects.requireNonNull(genBankAccession);
        this.refSeqAccession = Objects.requireNonNull(refSeqAccession);
        this.ucscName = Objects.requireNonNull(ucscName);
    }

    public static DefaultContig of(int id, String name, SequenceRole sequenceRole, int length, String genBankAccession, String refSeqAccession, String ucscName) {
        return new DefaultContig(id, name, sequenceRole, length, genBankAccession, refSeqAccession, ucscName);
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
        return genBankAccession;
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
        if (!(o instanceof DefaultContig)) return false;
        DefaultContig that = (DefaultContig) o;
        return id == that.id &&
                length == that.length &&
                name.equals(that.name) &&
                sequenceRole == that.sequenceRole &&
                genBankAccession.equals(that.genBankAccession) &&
                refSeqAccession.equals(that.refSeqAccession) &&
                ucscName.equals(that.ucscName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sequenceRole, length, genBankAccession, refSeqAccession, ucscName);
    }

    @Override
    public String toString() {
        return "ContigDefault{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sequenceRole=" + sequenceRole +
                ", length=" + length +
                ", genBankAccession='" + genBankAccession + '\'' +
                ", refSeqAccession='" + refSeqAccession + '\'' +
                ", ucscName='" + ucscName + '\'' +
                '}';
    }
}
