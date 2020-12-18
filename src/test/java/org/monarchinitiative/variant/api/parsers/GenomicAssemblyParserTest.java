package org.monarchinitiative.variant.api.parsers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.Contig;
import org.monarchinitiative.variant.api.GenomicAssembly;
import org.monarchinitiative.variant.api.SequenceRole;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GenomicAssemblyParserTest {

    private static final GenomicAssembly instance = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));

    // 1	assembled-molecule	1	Chromosome	CM000663.1	=	NC_000001.10	Primary Assembly	249250621	chr1
    private static final Contig chr1b37 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 249250621, "CM000663.1" , "NC_000001.10", "chr1");
    // MT	assembled-molecule	MT	Mitochondrion	J01415.2	=	NC_012920.1	non-nuclear	16569	chrM
    private static final Contig chrM = Contig.of(25, "MT", SequenceRole.ASSEMBLED_MOLECULE, 16569, "J01415.2" , "NC_012920.1", "chrM");

    @Test
    public void name() {
        assertThat(instance.name(), equalTo("GRCh37.p13"));
    }

    @Test
    public void submitter() {
        assertThat(instance.submitter(), equalTo("Genome Reference Consortium"));
    }

    @Test
    public void date() {
        assertThat(instance.date(), equalTo("2013-06-28"));
    }

    @Test
    public void organismName() {
        assertThat(instance.organismName(), equalTo("Homo sapiens (human)"));
    }

    @Test
    public void taxId() {
        assertThat(instance.taxId(), equalTo("9606"));
    }

    @Test
    public void genBankAccession() {
        assertThat(instance.genBankAccession(), equalTo("GCA_000001405.14"));
    }

    @Test
    public void refSeqAccession() {
        assertThat(instance.refSeqAccession(), equalTo("GCF_000001405.25"));
    }

    @Test
    public void contigById0() {
        assertThat(instance.contigById(0), equalTo(Contig.unknown()));
    }

    @Test
    public void contigById999() {
        assertThat(instance.contigById(999), equalTo(Contig.unknown()));
    }

    @Test
    public void contigById1() {
        assertThat(instance.contigById(1), equalTo(chr1b37));
    }
    @Test
    public void contigById25() {
        assertThat(instance.contigById(25), equalTo(chrM));
    }

    @Test
    public void contigByNameUnknown() {
        assertThat(instance.contigByName("wibble"), equalTo(Contig.unknown()));
    }

    @Test
    public void contigByName1() {
        assertThat(instance.contigByName("1"), equalTo(chr1b37));
    }

    @Test
    public void contigByNameMt() {
        assertThat(instance.contigByName("MT"), equalTo(chrM));
    }
}