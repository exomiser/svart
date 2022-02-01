package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    public void ofConstructor() {
        GenomicAssembly genomicAssembly = GenomicAssembly.of("RavBugblat01",
                "Ravenous Bugblatter", "000000", "Sirius Cybernetics Corporation", "2049-01-28",
                "GA_000000000", "GF_0000000000", List.of(TestContig.of(1, 45566579)));

        assertThat(genomicAssembly.name(), equalTo("RavBugblat01"));
        assertThat(genomicAssembly.organismName(), equalTo("Ravenous Bugblatter"));
        assertThat(genomicAssembly.taxId(), equalTo("000000"));
        assertThat(genomicAssembly.submitter(), equalTo("Sirius Cybernetics Corporation"));
        assertThat(genomicAssembly.date(), equalTo("2049-01-28"));
        assertThat(genomicAssembly.genBankAccession(), equalTo("GA_000000000"));
        assertThat(genomicAssembly.refSeqAccession(), equalTo("GF_0000000000"));
        assertThat(genomicAssembly.contigs(), equalTo(Set.of(TestContig.of(1, 45566579))));
    }
}