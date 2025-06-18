package org.monarchinitiative.svart.assembly;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

class GenomicAssembliesTest {

    @Test
    void GRCh37p13() {
        GenomicAssembly grch37 = GenomicAssemblies.GRCh37p13();
        assertThat(grch37.refSeqAccession(), equalTo("GCF_000001405.25"));
        assertThat(grch37.genBankAccession(), equalTo("GCA_000001405.14"));
        assertSame(grch37, GenomicAssemblies.GRCh37p13());
    }

    @Test
    void GRCh38p13() {
        GenomicAssembly grch38 = GenomicAssemblies.GRCh38p13();
        assertThat(grch38.refSeqAccession(), equalTo("GCF_000001405.39"));
        assertThat(grch38.genBankAccession(), equalTo("GCA_000001405.28"));
        assertSame(grch38, GenomicAssemblies.GRCh38p13());
    }

    @Test
    void T2T_CHM13v2() {
        GenomicAssembly t2t_chm13 = GenomicAssemblies.T2T_CHM13v2_0();
        assertThat(t2t_chm13.refSeqAccession(), equalTo("GCF_009914755.1"));
        assertThat(t2t_chm13.genBankAccession(), equalTo("GCA_009914755.4"));
        assertSame(t2t_chm13, GenomicAssemblies.T2T_CHM13v2_0());
    }

    @Test
    void GRCm38p6() {
        GenomicAssembly grcm38p6 = GenomicAssemblies.GRCm38p6();
        assertThat(grcm38p6.refSeqAccession(), equalTo("GCF_000001635.26"));
        assertThat(grcm38p6.genBankAccession(), equalTo("GCA_000001635.8"));
        assertSame(grcm38p6, GenomicAssemblies.GRCm38p6());
    }

    @Test
    void GRCm39() {
        GenomicAssembly grcm39 = GenomicAssemblies.GRCm39();
        assertThat(grcm39.refSeqAccession(), equalTo("GCF_000001635.27"));
        assertThat(grcm39.genBankAccession(), equalTo("GCA_000001635.9"));
        assertSame(grcm39, GenomicAssemblies.GRCm39());
    }

    @Test
    void downloadAssemblyIncorrectAccession() {
        var exception = assertThrows(IllegalArgumentException.class, () -> GenomicAssemblies.downloadAssembly("Wibble!"));
        assertThat(exception.getMessage(), equalTo("Invalid GenBank/RefSeq assembly accession Wibble!"));
    }

    @Disabled("Requires network access")
    @Nested
    class DownloadTests {

        @Test
        void testNoSuchAssembly() {
            var exception = assertThrows(IllegalStateException.class, () -> GenomicAssemblies.downloadAssembly("GCF_000000000.00"));
            assertThat(exception.getMessage(), equalTo("java.io.IOException: sun.net.ftp.FtpProtocolException: CWD 000:550 000: No such file or directory\n"));
        }

        @Test
        void downloadAssemblyRefSeqAccession() {
            GenomicAssembly GCF_000001405_25 = GenomicAssemblies.downloadAssembly("GCF_000001405.25");
            assertThat(GCF_000001405_25.refSeqAccession(), equalTo("GCF_000001405.25"));
            assertThat(GCF_000001405_25, equalTo(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"))));
        }

        @Test
        void downloadAssemblyGenBankAccession() {
            GenomicAssembly GCA_000001405_14 = GenomicAssemblies.downloadAssembly("GCA_000001405.14");
            assertThat(GCA_000001405_14.genBankAccession(), equalTo("GCA_000001405.14"));
            assertThat(GCA_000001405_14.contigs(), equalTo(GenomicAssembly.readAssembly(Path.of("src/test/resources/GCA_000001405.14_GRCh37.p13_assembly_report.txt")).contigs()));
        }

        @Test
        void downloadAssemblyToLocalFile(@TempDir Path tempDir) throws IOException {
            Path downloaded = GenomicAssemblies.downloadAssembly("GCA_000001405.14", tempDir);
            assertTrue(Files.exists(downloaded));
            assertThat(Files.size(downloaded), greaterThan(0L));
            GenomicAssembly genomicAssembly = GenomicAssembly.readAssembly(downloaded);
            assertThat(genomicAssembly.name(), equalTo("GRCh37.p13"));
        }

        /**
         * Need a new assembly report for GenomicAssemblies to load from resources? This method is what you want. Just
         * update the assemblyToAdd and assemblyName variables from
         * <a href="https://www.ncbi.nlm.nih.gov/datasets/genome/">NCBI Genome resources</a>, then update the
         * {@link GenomicAssemblies} implementation to load the file.
         *
         * @throws IOException
         */
        @Disabled("Helper method for updating assembly report resources")
        @Test
        void downloadNewAssemblyToResources() throws IOException {
            String assemblyToAdd = "GCF_009914755.1";
            String assemblyName = "T2T-CHM13v2.0";

            Path resourcePath = Path.of("src/main/resources/org/monarchinitiative/svart/assemblies");
            Path downloaded = GenomicAssemblies.downloadAssembly(assemblyToAdd, resourcePath);
            assertTrue(Files.exists(downloaded));
            assertThat(Files.size(downloaded), greaterThan(0L));
            GenomicAssembly genomicAssembly = GenomicAssembly.readAssembly(downloaded);
            assertThat(genomicAssembly.name(), equalTo(assemblyName));
        }
    }


    @Test
    void testListAssemblies() {
        assertThat(GenomicAssemblies.listAssemblies(), equalTo(
                Set.of("GCA_000001405.14", "GCF_000001405.25", // GRCh37
                        "GCA_000001405.28", "GCF_000001405.39", // GRCh38
                        "GCA_009914755.4", "GCF_009914755.1", // T2T-CHM13
                        "GCA_000001635.8", "GCF_000001635.26", // GRCm38
                        "GCA_000001635.9", "GCF_000001635.27") // GRCm39
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "GCA_000001405.01, false",
            "GCF_000001405.25, true",
            "GCA_000001405.14, true",
            "GCF_000001405.39, true",
            "GCA_000001405.28, true",
            "GCA_009914755.4, true",
            "GCF_009914755.1, true",
            "GCA_000001405.29, false"
    })
    void testContainsAssembly(String assemblyAccession, boolean expected) {
        assertThat(GenomicAssemblies.containsAssembly(assemblyAccession), equalTo(expected));
    }

    @Disabled("Requires external network access")
    @Test
    void testGetAssemblyMissing() {
        var assembly = GenomicAssemblies.getAssembly("GCA_000001405.29");
        var cached = GenomicAssemblies.getAssembly("GCA_000001405.29");
        assertThat(assembly, equalTo(cached));
        assertThat(GenomicAssemblies.containsAssembly("GCA_000001405.29"), equalTo(true));
    }

    @Test
    void testGetAssemblyIncorrectAccession() {
        var exception = assertThrows(IllegalArgumentException.class, () -> GenomicAssemblies.getAssembly("Wibble!"));
        assertThat(exception.getMessage(), equalTo("Invalid GenBank/RefSeq assembly accession Wibble!"));
    }

    @Test
    void testGetAssemblyLocalCache() {
        var GCF_000001405_25 = GenomicAssemblies.getAssembly("GCF_000001405.25");
        var GCA_000001405_14 = GenomicAssemblies.getAssembly("GCA_000001405.14");
        assertThat(GCF_000001405_25, equalTo(GCA_000001405_14));
        assertThat(GCF_000001405_25, equalTo(GenomicAssemblies.GRCh37p13()));
        assertThat(GCA_000001405_14, equalTo(GenomicAssemblies.GRCh37p13()));
    }

}