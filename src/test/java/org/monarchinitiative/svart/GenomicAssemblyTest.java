package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class GenomicAssemblyTest {

    @Test
    public void readFromPath() {
        assertThat(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt")).name(), equalTo("GRCh37.p13"));
    }

    @Test
    public void readFromStream() throws IOException {
        Path assemblyPath = Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt");
        assertThat(GenomicAssembly.readAssembly(Files.newInputStream(assemblyPath)).name(), equalTo("GRCh37.p13"));
    }

    @Test
    public void fromScratch() {
        List<Contig> contigs = List.of(
                Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 248_956_422, "CM000663.2", "NC_000001.11", "chr1"),
                Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, "2", AssignedMoleculeType.CHROMOSOME, 248_956_422, "CM000664.2", "NC_000002.12", "chr2")
        );
        GenomicAssembly assembly = GenomicAssembly.of("GRCh38.p13", "Homo sapiens (human)", "9606",
                "Genome Reference Consortium", "2019-02-28",
                "GCA_000001405.28", "GCF_000001405.39",
                contigs);

        assertThat(assembly.name(), equalTo("GRCh38.p13"));
        assertThat(assembly.organismName(), equalTo("Homo sapiens (human)"));
        assertThat(assembly.taxId(), equalTo("9606"));
        assertThat(assembly.submitter(), equalTo("Genome Reference Consortium"));
        assertThat(assembly.date(), equalTo("2019-02-28"));
        assertThat(assembly.genBankAccession(), equalTo("GCA_000001405.28"));
        assertThat(assembly.refSeqAccession(), equalTo("GCF_000001405.39"));

        assertThat(assembly.contigs(), hasSize(2));
        assertThat(assembly.contigByName("1"), equalTo(contigs.get(0)));
        assertThat(assembly.contigByName("2"), equalTo(contigs.get(1)));
    }
}