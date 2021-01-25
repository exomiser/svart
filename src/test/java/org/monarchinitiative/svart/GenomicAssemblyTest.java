package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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


}