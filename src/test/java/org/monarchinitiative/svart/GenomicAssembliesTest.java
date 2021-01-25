package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GenomicAssembliesTest {

    @Test
    public void readFromPath() {
        assertThat(GenomicAssemblies.read(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt")).name(), equalTo("GRCh37.p13"));
    }

    @Test
    public void readFromStream() throws IOException {
        Path assemblyPath = Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt");
        assertThat(GenomicAssemblies.read(Files.newInputStream(assemblyPath)).name(), equalTo("GRCh37.p13"));
    }

}