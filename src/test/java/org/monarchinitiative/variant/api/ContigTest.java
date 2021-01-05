package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;


public class ContigTest {

    @Test
    public void properties() {
        Contig contig = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 10, "CM000663.1", "NC_000001.10", "chr1");
        assertThat(contig.id(), equalTo(1));
        assertThat(contig.name(), equalTo("1"));
        assertThat(contig.sequenceRole(), equalTo(SequenceRole.ASSEMBLED_MOLECULE));
        assertThat(contig.assignedMolecule(), equalTo("1"));
        assertThat(contig.assignedMoleculeType(), equalTo(AssignedMoleculeType.CHROMOSOME));
        assertThat(contig.length(), equalTo(10));
        assertThat(contig.genBankAccession(), equalTo("CM000663.1"));
        assertThat(contig.refSeqAccession(), equalTo("NC_000001.10"));
        assertThat(contig.ucscName(), equalTo("chr1"));
        assertThat(contig.isKnownContig(), equalTo(true));
        assertThat(contig.isUnknownContig(), equalTo(false));
    }

}