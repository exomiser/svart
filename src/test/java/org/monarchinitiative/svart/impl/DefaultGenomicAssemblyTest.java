package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.AssignedMoleculeType;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.monarchinitiative.svart.assembly.GenomicAssemblyParser;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultGenomicAssemblyTest {


    private static final GenomicAssembly grch37p13 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));

    // 1	assembled-molecule	1	Chromosome	CM000663.1	=	NC_000001.10	Primary Assembly	249250621	chr1
    private static final Contig chr1b37 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 249250621, "CM000663.1" , "NC_000001.10", "chr1");
    private static final Contig chr1b38 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 248956422, "CM000663.2" , "NC_000001.11", "chr1");
    // MT	assembled-molecule	MT	Mitochondrion	J01415.2	=	NC_012920.1	non-nuclear	16569	chrM
    private static final Contig chrM = Contig.of(25, "MT", SequenceRole.ASSEMBLED_MOLECULE, "MT", AssignedMoleculeType.MITOCHONDRION, 16569, "J01415.2" , "NC_012920.1", "chrM");
    //HSCHR17_1_CTG5	alt-scaffold	17	Chromosome	GL000258.1	=	NT_167251.1	ALT_REF_LOCI_9	1680828	chr17_ctg5_hap1
    private static final Contig chr17_ctg5_hap1 = Contig.of(297, "HSCHR17_1_CTG5", SequenceRole.ALT_SCAFFOLD, "17", AssignedMoleculeType.CHROMOSOME, 1680828, "GL000258.1", "NT_167251.1", "chr17_ctg5_hap1");

    @Test
    public void error() {
        assertThrows(RuntimeException.class, ()-> GenomicAssemblyParser.parseAssembly(Path.of("wibble")));
    }

    @Test
    public void name() {
        assertThat(grch37p13.name(), equalTo("GRCh37.p13"));
    }

    @Test
    public void submitter() {
        assertThat(grch37p13.submitter(), equalTo("Genome Reference Consortium"));
    }

    @Test
    public void date() {
        assertThat(grch37p13.date(), equalTo("2013-06-28"));
    }

    @Test
    public void organismName() {
        assertThat(grch37p13.organismName(), equalTo("Homo sapiens (human)"));
    }

    @Test
    public void taxId() {
        assertThat(grch37p13.taxId(), equalTo("9606"));
    }

    @Test
    public void genBankAccession() {
        assertThat(grch37p13.genBankAccession(), equalTo("GCA_000001405.14"));
    }

    @Test
    public void refSeqAccession() {
        assertThat(grch37p13.refSeqAccession(), equalTo("GCF_000001405.25"));
    }

    @Test
    public void contigByNegativeId() {
        assertThat(grch37p13.contigById(-1), equalTo(Contig.unknown()));
    }

    @Test
    public void contigById0() {
        assertThat(grch37p13.contigById(0), equalTo(Contig.unknown()));
    }

    @Test
    public void contigById999() {
        assertThat(grch37p13.contigById(999), equalTo(Contig.unknown()));
    }

    @Test
    public void contigById1() {
        assertThat(grch37p13.contigById(1), equalTo(chr1b37));
    }

    @Test
    public void contigById25() {
        assertThat(grch37p13.contigById(25), equalTo(chrM));
    }

    @ParameterizedTest
    @CsvSource({
            "na", "wibble"
    })
    public void contigByName_unknown(String name) {
        assertThat(grch37p13.contigByName(name), equalTo(Contig.unknown()));
    }

    @ParameterizedTest
    @CsvSource({
            "1", "CM000663.1" , "NC_000001.10", "chr1"
    })
    public void contigByName_chr1(String name) {
        assertThat(grch37p13.contigByName(name), equalTo(chr1b37));
    }

    @ParameterizedTest
    @CsvSource({
            "MT", "J01415.2" , "NC_012920.1", "chrM"
    })
    public void contigByName_chrM(String name) {
        assertThat(grch37p13.contigByName(name), equalTo(chrM));
    }

    @ParameterizedTest
    @CsvSource({
            "HSCHR17_1_CTG5", "GL000258.1", "NT_167251.1", "chr17_ctg5_hap1"
    })
    public void contigByName_HSCHR17_1_CTG5(String name) {
        assertThat(grch37p13.contigByName(name), equalTo(chr17_ctg5_hap1));
    }

    @Test
    public void contigs() {
        assertThat(grch37p13.contigs().size(), equalTo(297));
    }
    @Test
    public void doesNotContainContigUnknown() {
        assertFalse(grch37p13.containsContig(Contig.unknown()));
    }

    @Test
    public void contains_chr1() {
        assertTrue(grch37p13.containsContig(chr1b37));
    }

    @Test
    public void does_not_contain_chr1_wrong_assembly() {
        assertFalse(grch37p13.containsContig(chr1b38));
    }

    @Test
    public void contains_chrM() {
        assertTrue(grch37p13.containsContig(chrM));
    }

    @Test
    public void contains_chr17_ctg5_hap1() {
        assertTrue(grch37p13.containsContig(chr17_ctg5_hap1));
    }

    @Test
    public void shouldNotContainDuplicates() {
        Contig one = TestContig.of(1, 249250621);
        Contig two = TestContig.of(2, 398325701);
        Contig three = TestContig.of(3, 498325701);
        GenomicAssembly instance = DefaultGenomicAssembly.builder().contigs(List.of(three, one, two, two)).build();
        assertThat(instance.contigById(1), equalTo(one));
        assertThat(instance.contigById(2), equalTo(two));
        assertThat(instance.contigById(3), equalTo(three));
        assertThat(instance.contigs(), equalTo(Set.of(one, two, three)));
    }

    @Test
    public void returnsOrderedContigs() {
        Contig one = TestContig.of(1, 249250621);
        Contig two = TestContig.of(2, 398325701);
        Contig three = TestContig.of(3, 498325701);
        GenomicAssembly instance = DefaultGenomicAssembly.builder().contigs(List.of(three, one, two, two)).build();

        List<Contig> actual = List.copyOf(instance.contigs());
        assertThat(actual, equalTo(List.of(one, two, three)));
    }

    @Test
    public void throwsExceptionWithDuplicatedIds() {
        Contig firstNo2Contig = TestContig.of(2, 249250621);
        Contig otherNo2Contig = TestContig.of(2, 398325701);
        assertThrows(IllegalStateException.class, () -> DefaultGenomicAssembly.builder().contigs(List.of(chr1b37, firstNo2Contig, otherNo2Contig)).build());
    }

    @Test
    public void throwsExceptionWithNonSequentialIds() {
        Contig one = TestContig.of(1, 249250621);
        Contig three = TestContig.of(3, 398325701);
        assertThrows(IllegalStateException.class, () -> DefaultGenomicAssembly.builder().contigs(List.of(one, three)).build());
    }

    @Test
    public void throwsExceptionWithNonUnknownZeroId() {
        Contig zero = TestContig.of(0, 249250621);
        Contig one = TestContig.of(1, 398325701);
        assertThrows(IllegalArgumentException.class, () -> DefaultGenomicAssembly.builder().contigs(List.of(zero, one)).build());
    }

    @Test
    public void doesNotIncludeNaNames() {
        Contig naNameContig = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, "2", AssignedMoleculeType.CHROMOSOME, 249250621, "na" , "na", "na");
        GenomicAssembly instance = DefaultGenomicAssembly.builder().contigs(List.of(chr1b37, naNameContig)).build();
        assertThat(instance.contigByName("na"), equalTo(Contig.unknown()));
        assertThat(instance.contigByName("2"), equalTo(naNameContig));
    }

    @Test
    public void doesNotIncludeAssignedMoleculeNamesForNonAssembledMolecules() {
        Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, "2", AssignedMoleculeType.CHROMOSOME, 243199373, "CM000664.1" , "NC_000002.11", "chr2");
        Contig chr2Patch1007 = Contig.of(3, "HG1007_PATCH", SequenceRole.FIX_PATCH, "2", AssignedMoleculeType.CHROMOSOME, 66021, "GL877870.2" , "NW_003571031.1", "na");

        GenomicAssembly instance = DefaultGenomicAssembly.builder().contigs(List.of(chr1b37, chr2, chr2Patch1007)).build();
        assertThat(instance.contigByName("2"), equalTo(chr2));
        assertThat(instance.contigByName("HG1007_PATCH"), equalTo(chr2Patch1007));
    }

    @Test
    public void emptyAssembly() {
        GenomicAssembly instance = DefaultGenomicAssembly.builder().build();
        assertThat(instance.contigs().size(), equalTo(0));
        assertThat(instance.containsContig(Contig.unknown()), equalTo(false));
    }
}