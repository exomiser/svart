package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.AssignedMoleculeType;
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
    private final String assignedMolecule;
    private final AssignedMoleculeType assignedMoleculeType;
    private final int length;
    private final String genBankAccession;
    private final String refSeqAccession;
    private final String ucscName;

    private DefaultContig(int id, String name, SequenceRole sequenceRole, String assignedMolecule, AssignedMoleculeType assignedMoleculeType, int length, String genBankAccession, String refSeqAccession, String ucscName) {
        this.id = requirePositiveNonZeroId(id);
        this.name = Objects.requireNonNull(name);
        this.sequenceRole = Objects.requireNonNull(sequenceRole);
        this.assignedMolecule = Objects.requireNonNull(assignedMolecule);
        this.assignedMoleculeType = Objects.requireNonNull(assignedMoleculeType);
        this.length = requirePositiveNonZeroLength(length);
        this.genBankAccession = Objects.requireNonNull(genBankAccession);
        this.refSeqAccession = Objects.requireNonNull(refSeqAccession);
        this.ucscName = Objects.requireNonNull(ucscName);
    }

    private int requirePositiveNonZeroId(int id) {
        if (id == 0) {
            throw new IllegalArgumentException("id 0 is reserved for the unknown contig");
        }
        if (id < 0) {
            throw new IllegalArgumentException("id must have a positive value");
        }
        return id;
    }

    private int requirePositiveNonZeroLength(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Contig id " + id + " (name='" + name + "') must have a length greater than zero");
        }
        return length;
    }

    public static DefaultContig of(int id, String name, SequenceRole sequenceRole, String assignedMolecule, AssignedMoleculeType assignedMoleculeType, int length, String genBankAccession, String refSeqAccession, String ucscName) {
        return new DefaultContig(id, name, sequenceRole, assignedMolecule, assignedMoleculeType, length, genBankAccession, refSeqAccession, ucscName);
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
    public String assignedMolecule() {
        return assignedMolecule;
    }

    @Override
    public AssignedMoleculeType assignedMoleculeType() {
        return assignedMoleculeType;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultContig)) return false;
        DefaultContig that = (DefaultContig) o;
        return id == that.id &&
                length == that.length &&
                name.equals(that.name) &&
                sequenceRole == that.sequenceRole &&
                assignedMolecule.equals(that.assignedMolecule) &&
                assignedMoleculeType == that.assignedMoleculeType &&
                genBankAccession.equals(that.genBankAccession) &&
                refSeqAccession.equals(that.refSeqAccession) &&
                ucscName.equals(that.ucscName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sequenceRole, assignedMolecule, assignedMoleculeType, length, genBankAccession, refSeqAccession, ucscName);
    }

    @Override
    public String toString() {
        return "Contig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sequenceRole=" + sequenceRole +
                ", assignedMolecule=" + assignedMolecule +
                ", assignedMoleculeType=" + assignedMoleculeType +
                ", length=" + length +
                ", genBankAccession='" + genBankAccession + '\'' +
                ", refSeqAccession='" + refSeqAccession + '\'' +
                ", ucscName='" + ucscName + '\'' +
                '}';
    }
}
