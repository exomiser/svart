package org.monarchinitiative.svart;

import org.monarchinitiative.svart.assembly.AssignedMoleculeType;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.monarchinitiative.svart.impl.DefaultContig;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Contig extends Comparable<Contig> {

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file.
    // *MUST* be natural ordering of [autosomes] [sex-chromosomes] [mitochondrial]
    // *MUST* be unique within an assembly
    // Other unplaced, unlocalised, patch, alt-scaffold sequences are not required to be ordered in any specific way.
    // Zero is reserved as the 'unknown' value.
    int id();

    // Sequence-name column 0 (zero-based) of the assembly report file e.g. 1-22, X,Y,MT
    String name();

    /**
     * @return contig sequence role
     */
    SequenceRole sequenceRole();

    // Assigned-Molecule column 2 (zero-based) of the assembly report file e.g. 1-22, X,Y,MT. This may be a duplicate of
    // the sequenceName field for chromosomes, but for unlocalised scaffolds, alt loci, patches etc will point to the chromosome on which they
    // are located.
    String assignedMolecule();

    // Assigned-Molecule-Location/Type column 3 (zero-based) of the assembly report file. One of Chromosome, Mitochondrion, na
    AssignedMoleculeType assignedMoleculeType();

    // Sequence-Length column 8 (zero-based) of assembly-report
    int length();

    // The Genbank identifier for the contig. This should not be empty and is the primary source of the sequence.
    // GenBank-Accn column 4 (zero-based) of assembly-report
    String genBankAccession();

    // The RefSeq accession of the contig or empty if unknown
    // RefSeq is recommended by the HGVS for use when reporting variants.
    // http://varnomen.hgvs.org/bg-material/refseq/
    // RefSeq-Accn column 6 (zero-based) of assembly-report
    String refSeqAccession();

    // Because chr prefixes are awesome
    // UCSC-style-name column 9 (zero-based) of assembly-report
    String ucscName();

    //# Sequence-Name	Sequence-Role	Assigned-Molecule	Assigned-Molecule-Location/Type	GenBank-Accn	Relationship	RefSeq-Accn	Assembly-Unit	Sequence-Length	UCSC-style-name
    // HUMAN (GRCh38.p13)
    // 1	assembled-molecule	1	Chromosome	CM000663.2	=	NC_000001.11	Primary Assembly	248956422	chr1
    // 22	assembled-molecule	22	Chromosome	CM000684.2	=	NC_000022.11	Primary Assembly	50818468	chr22
    // X	assembled-molecule	X	Chromosome	CM000685.2	=	NC_000023.11	Primary Assembly	156040895	chrX
    // Y	assembled-molecule	Y	Chromosome	CM000686.2	=	NC_000024.10	Primary Assembly	57227415	chrY
    // MT	assembled-molecule	MT	Mitochondrion	J01415.2	=	NC_012920.1	non-nuclear	16569	chrM
    //
    // CHIMP (Pan_tro 3.0)
    // chr1	assembled-molecule	1	Chromosome	CM000314.3	=	NC_006468.4	Primary Assembly	228573443	chr1
    // chr22	assembled-molecule	22	Chromosome	CM000335.3	=	NC_006489.4	Primary Assembly	37823149	chr22
    // chrX	assembled-molecule	X	Chromosome	CM000336.3	=	NC_006491.4	Primary Assembly	155549662	chrX
    // BAC_CHR_Y_V2	assembled-molecule	Y	Chromosome	DP000054.3	=	NC_006492.4	Primary Assembly	26350515	chrY
    // MT	assembled-molecule	MT	Mitochondrion	na	<>	NC_001643.1	non-nuclear	16554	na
    //
    // RAT (Rrattus_CSIRO_v1)
    // chr1	assembled-molecule	1	Chromosome	CM021548.1	=	NC_046154.1	Primary Assembly	272949890	na
    // chr18	assembled-molecule	18	Chromosome	CM021565.1	=	NC_046171.1	Primary Assembly	49846535	na
    // chrX	assembled-molecule	X	Chromosome	CM021566.1	=	NC_046172.1	Primary Assembly	130373513	na
    // chrY	assembled-molecule	Y	Chromosome	CM021567.1	=	NC_046173.1	Primary Assembly	2293298	na
    // MT	assembled-molecule	MT	Mitochondrion	na	<>	NC_012374.1	non-nuclear	16305	na
    //
    // MOUSE (GRCm39)
    // 1	assembled-molecule	1	Chromosome	CM000994.3	=	NC_000067.7	C57BL/6J	195154279	na
    // 19	assembled-molecule	19	Chromosome	CM001012.3	=	NC_000085.7	C57BL/6J	61420004	na
    // X	assembled-molecule	X	Chromosome	CM001013.3	=	NC_000086.8	C57BL/6J	169476592	na
    // Y	assembled-molecule	Y	Chromosome	CM001014.3	=	NC_000087.8	C57BL/6J	91455967	na
    // MT	assembled-molecule	MT	Mitochondrion	AY172335.1	=	NC_005089.1	non-nuclear	16299	chrM

    int hashCode();

    boolean equals(Object o);

    static Contig of(int id, String name, SequenceRole sequenceRole, String assignedMolecule, AssignedMoleculeType assignedMoleculeType, int length, String genbankAccession, String refSeqAccession, String ucscName) {
        return DefaultContig.of(id, name, sequenceRole, assignedMolecule, assignedMoleculeType, length, genbankAccession, refSeqAccession, ucscName);
    }

    /**
     * A null-object Contig of length zero.
     *
     * @return The Unknown {@link Contig}
     */
    static Contig unknown() {
        return UnknownContig.instance();
    }

    /**
     * Returns true if the Contig is the Unknown contig
     */
    default boolean isUnknown() {
        return this.equals(unknown());
    }

    @Override
    default int compareTo(Contig o) {
        return compare(this, o);
    }

    static int compare(Contig x, Contig y) {
        return Integer.compare(x.id(), y.id());
    }
}
