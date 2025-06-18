package org.monarchinitiative.svart.assembly;

import org.monarchinitiative.svart.Contig;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public record DefaultContig(
        int id,
        String name,
        SequenceRole sequenceRole,
        String assignedMolecule,
        AssignedMoleculeType assignedMoleculeType,
        int length,
        String genBankAccession,
        String refSeqAccession,
        String ucscName
) implements Contig {

    public DefaultContig(int id, String name, SequenceRole sequenceRole, String assignedMolecule, AssignedMoleculeType assignedMoleculeType, int length, String genBankAccession, String refSeqAccession, String ucscName) {
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
