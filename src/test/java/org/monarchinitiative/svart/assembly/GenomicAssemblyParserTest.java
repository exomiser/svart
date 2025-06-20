package org.monarchinitiative.svart.assembly;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.Contig;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenomicAssemblyParserTest {

    private static final GenomicAssembly grch37p13 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));

    // 1	assembled-molecule	1	Chromosome	CM000663.1	=	NC_000001.10	Primary Assembly	249250621	chr1
    private static final Contig chr1b37 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 249250621, "CM000663.1" , "NC_000001.10", "chr1");
    // MT	assembled-molecule	MT	Mitochondrion	J01415.2	=	NC_012920.1	non-nuclear	16569	chrM
    private static final Contig chrM = Contig.of(25, "MT", SequenceRole.ASSEMBLED_MOLECULE, "MT", AssignedMoleculeType.MITOCHONDRION, 16569, "J01415.2" , "NC_012920.1", "chrM");

    @Test
    void error() {
        assertThrows(RuntimeException.class, () -> GenomicAssemblyParser.parseAssembly(Path.of("wibble")));
    }

    @Test
    void name() {
        assertThat(grch37p13.name(), equalTo("GRCh37.p13"));
    }

    @Test
    void submitter() {
        assertThat(grch37p13.submitter(), equalTo("Genome Reference Consortium"));
    }

    @Test
    void date() {
        assertThat(grch37p13.date(), equalTo("2013-06-28"));
    }

    @Test
    void organismName() {
        assertThat(grch37p13.organismName(), equalTo("Homo sapiens (human)"));
    }

    @Test
    void taxId() {
        assertThat(grch37p13.taxId(), equalTo("9606"));
    }

    @Test
    void genBankAccession() {
        assertThat(grch37p13.genBankAccession(), equalTo("GCA_000001405.14"));
    }

    @Test
    void refSeqAccession() {
        assertThat(grch37p13.refSeqAccession(), equalTo("GCF_000001405.25"));
    }

    @Test
    void contigByNegativeId() {
        assertThat(grch37p13.contigById(-1), equalTo(Contig.unknown()));
    }

    @Test
    void contigById0() {
        assertThat(grch37p13.contigById(0), equalTo(Contig.unknown()));
    }

    @Test
    void contigById999() {
        assertThat(grch37p13.contigById(999), equalTo(Contig.unknown()));
    }

    @Test
    void contigById1() {
        assertThat(grch37p13.contigById(1), equalTo(chr1b37));
    }
    @Test
    void contigById25() {
        assertThat(grch37p13.contigById(25), equalTo(chrM));
    }

    @Test
    void contigByNameUnknown() {
        assertThat(grch37p13.contigByName("wibble"), equalTo(Contig.unknown()));
    }

    @Test
    void contigByName1() {
        assertThat(grch37p13.contigByName("1"), equalTo(chr1b37));
    }

    @Test
    void contigByNameMt() {
        assertThat(grch37p13.contigByName("MT"), equalTo(chrM));
    }

    @Test
    void contigByNameChrM() {
        assertThat(grch37p13.contigByName("chrM"), equalTo(chrM));
    }

    @Test
    void contigByNameHSCHR17_1_CTG5() {
        //HSCHR17_1_CTG5	alt-scaffold	17	Chromosome	GL000258.1	=	NT_167251.1	ALT_REF_LOCI_9	1680828	chr17_ctg5_hap1
        Contig last = Contig.of(297, "HSCHR17_1_CTG5", SequenceRole.ALT_SCAFFOLD, "17", AssignedMoleculeType.CHROMOSOME, 1680828, "GL000258.1", "NT_167251.1", "chr17_ctg5_hap1");
        assertThat(grch37p13.contigByName("HSCHR17_1_CTG5"), equalTo(last));
    }

    @Test
    void contigs() {
        assertThat(grch37p13.contigs().size(), equalTo(297));
    }

    @Test
    void grcm38p6FromInputStream() throws Exception {
        GenomicAssembly mm10 = GenomicAssemblyParser.parseAssembly(Files.newInputStream(Path.of("src/test/resources/GCF_000001635.26_GRCm38.p6_assembly_report.txt")));
        assertThat(mm10.name(), equalTo("GRCm38.p6"));
        assertThat(mm10.organismName(), equalTo("Mus musculus (house mouse)"));
        assertThat(mm10.taxId(), equalTo("10090"));
        assertThat(mm10.contigByName("1").id(), equalTo(1));
        assertThat(mm10.contigByName("19").id(), equalTo(19));
        assertThat(mm10.contigByName("X").id(), equalTo(20));
        assertThat(mm10.contigByName("Y").id(), equalTo(21));
        assertThat(mm10.contigByName("MT").id(), equalTo(22));
    }

    @Test
    void grcm38p6() {
        GenomicAssembly mm10 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001635.26_GRCm38.p6_assembly_report.txt"));
        assertThat(mm10.name(), equalTo("GRCm38.p6"));
        assertThat(mm10.organismName(), equalTo("Mus musculus (house mouse)"));
        assertThat(mm10.taxId(), equalTo("10090"));
        assertThat(mm10.contigByName("1").id(), equalTo(1));
        assertThat(mm10.contigByName("19").id(), equalTo(19));
        assertThat(mm10.contigByName("X").id(), equalTo(20));
        assertThat(mm10.contigByName("Y").id(), equalTo(21));
        assertThat(mm10.contigByName("MT").id(), equalTo(22));
    }

    @Test
    void chimpPTRv2() {
        GenomicAssembly clint_PTRv2 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_002880755.1_Clint_PTRv2_assembly_report.txt"));
        assertThat(clint_PTRv2.name(), equalTo("Clint_PTRv2"));
        assertThat(clint_PTRv2.organismName(), equalTo("Pan troglodytes (chimpanzee)"));
        assertThat(clint_PTRv2.taxId(), equalTo("9598"));
        assertThat(clint_PTRv2.contigByName("chr1").id(), equalTo(1));
        assertThat(clint_PTRv2.contigByName("chr22").id(), equalTo(23));
        assertThat(clint_PTRv2.contigByName("chrX").id(), equalTo(24));
        assertThat(clint_PTRv2.contigByName("Y").id(), equalTo(25));
        assertThat(clint_PTRv2.contigByName("MT").id(), equalTo(26));
    }

    @Test
    void riceIRGSP1() {
        GenomicAssembly IRGSP_1 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_001433935.1_IRGSP-1.0_assembly_report.txt"));
        assertThat(IRGSP_1.name(), equalTo("IRGSP-1.0"));
        assertThat(IRGSP_1.organismName(), equalTo("Oryza sativa Japonica Group (Japanese rice)"));
        assertThat(IRGSP_1.taxId(), equalTo("39947"));
        assertThat(IRGSP_1.contigByName("1"), equalTo(IRGSP_1.contigById(1)));
        assertThat(IRGSP_1.contigByName("12"), equalTo(IRGSP_1.contigById(12)));

        assertThat(IRGSP_1.contigByName("Pltd"), equalTo(IRGSP_1.contigById(13)));
        assertThat(IRGSP_1.contigByName("Pltd").assignedMoleculeType(), equalTo(AssignedMoleculeType.CHLOROPLAST));

        assertThat(IRGSP_1.contigByName("MT"), equalTo(IRGSP_1.contigById(14)));

        assertThat(IRGSP_1.contigByName("B1"), equalTo(IRGSP_1.contigById(15)));
        assertThat(IRGSP_1.contigByName("B1").assignedMoleculeType(), equalTo(AssignedMoleculeType.MITOCHONDRIAL_PLASMID));
    }

}