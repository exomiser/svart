package org.monarchinitiative.svart;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class GenomicAssembliesTest {

    @Test
    public void GRCh37p13() {
        GenomicAssembly grch37 = GenomicAssemblies.GRCh37p13();
        assertThat(grch37.refSeqAccession(), equalTo("GCF_000001405.25"));
        assertThat(grch37.genBankAccession(), equalTo("GCA_000001405.14"));
        assertSame(grch37, GenomicAssemblies.GRCh37p13());
    }

    @Test
    public void GRCh38p13() {
        GenomicAssembly grch38 = GenomicAssemblies.GRCh38p13();
        assertThat(grch38.refSeqAccession(), equalTo("GCF_000001405.39"));
        assertThat(grch38.genBankAccession(), equalTo("GCA_000001405.28"));
        assertSame(grch38, GenomicAssemblies.GRCh38p13());
    }

    @Test
    public void GRCm38p6() {
        GenomicAssembly grcm38p6 = GenomicAssemblies.GRCm38p6();
        assertThat(grcm38p6.refSeqAccession(), equalTo("GCF_000001635.26"));
        assertThat(grcm38p6.genBankAccession(), equalTo("GCA_000001635.8"));
        assertSame(grcm38p6, GenomicAssemblies.GRCm38p6());
    }

    @Test
    public void GRCm39() {
        GenomicAssembly grcm39 = GenomicAssemblies.GRCm39();
        assertThat(grcm39.refSeqAccession(), equalTo("GCF_000001635.27"));
        assertThat(grcm39.genBankAccession(), equalTo("GCA_000001635.9"));
        assertSame(grcm39, GenomicAssemblies.GRCm39());
    }

    @Disabled("Requires external network access")
    @Test
    public void downloadAssemblyRefSeqAccession() {
        GenomicAssembly GCF_000001405_25 = GenomicAssemblies.downloadAssembly("GCF_000001405.25");
        assertThat(GCF_000001405_25.refSeqAccession(), equalTo("GCF_000001405.25"));
        assertThat(GCF_000001405_25, equalTo(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"))));
    }

    @Disabled("Requires external network access")
    @Test
    public void downloadAssemblyGenBankAccession() {
        GenomicAssembly GCA_000001405_14 = GenomicAssemblies.downloadAssembly("GCA_000001405.14");
        assertThat(GCA_000001405_14.genBankAccession(), equalTo("GCA_000001405.14"));
        assertThat(GCA_000001405_14.contigs(), equalTo(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCA_000001405.14_GRCh37.p13_assembly_report.txt")).contigs()));
    }
}