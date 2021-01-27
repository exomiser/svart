package org.monarchinitiative.svart;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class GenomicAssembliesTest {

    @Disabled("Requires external network access")
    @Test
    public void GRCh37p13() {
        GenomicAssemblies.GRCh37p13();
    }

    @Disabled("Requires external network access")
    @Test
    public void downloadAssembly() {
        GenomicAssembly GCF_000001405_25 = GenomicAssemblies.downloadAssembly("GCF_000001405.25");
        assertThat(GCF_000001405_25.refSeqAccession(), equalTo("GCF_000001405.25"));
        assertThat(GCF_000001405_25, equalTo(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"))));
    }

}