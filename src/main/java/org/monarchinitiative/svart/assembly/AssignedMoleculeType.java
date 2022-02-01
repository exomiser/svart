package org.monarchinitiative.svart.assembly;

/**
 * Probably incomplete enumeration of the NCBI assembly_report Assigned-Molecule-Location/Type field. These should be
 * sufficient for most common reference assemblies and are known to work for human, mouse, chimp, rice and soybean.
 *
 * The intention is that this class enables locating/identifying patch/alt/unplaced loci on the correct chromosome and
 * aiding identification of non-chromosomal/non-nuclear nucleic acid molecules in a genomic assembly.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum AssignedMoleculeType {

    CHROMOSOME("Chromosome"),
    MITOCHONDRION("Mitochondrion"),
    CHLOROPLAST("Chloroplast"),
    MITOCHONDRIAL_PLASMID("Mitochondrial Plasmid"),
    PLASMID("Plasmid"),
    SEGMENT("Segment"),
    LINKAGE_GROUP("Linkage Group"),
    UNKNOWN("na");

    private final String value;

    AssignedMoleculeType(String value) {
        this.value = value;
    }

    public static AssignedMoleculeType parseMoleculeType(String value) {
        for (AssignedMoleculeType moleculeType : AssignedMoleculeType.values()) {
            if (moleculeType.value.equals(value)) {
                return moleculeType;
            }
        }
        return UNKNOWN;
    }
}
