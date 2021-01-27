package org.monarchinitiative.svart.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.monarchinitiative.svart.*;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VcfConverterTest {

    private final GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
    Contig chr1 = b37.contigById(1);
    private final VcfConverter instance = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

    @Nested
    public class ConvertNonSymbolicTests {

        @Test
        void convert() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            Variant snv = instance.convert("chr1", "rs123456", 12345, "C", "T");
            assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));
        }

        @Test
        void trimsInput() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            Variant snv = instance.convert("chr1", "rs123456", 12345, "CCC", "TCC");
            assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));
        }

        @Test
        void throwsExceptionWithMultiAllelicSite() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	CCC	TC, TCC	6	PASS	.
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "CCC", "TC, TCC"));
            assertThat(exception.getMessage(), equalTo("Illegal multi-allelic alt allele TC, TCC"));
        }

        @Test
        void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "C", "<DEL>"));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele <DEL>"));
        }

        @Test
        void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "C", "C[2:321682["));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele C[2:321682["));
        }
    }

    @Nested
    public class ConvertSymbolicTests {

        @Test
        void convertSymbolic() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
            Variant ins = instance.convertSymbolic("chr1", "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
            assertThat(ins, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), Position.of(12345), "C", "<INS>", 200)));
        }

        @Test
        void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic("chr1", "rs123456", Position.of(12345), Position.of(12345), "C", "T", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic or breakend alt allele T"));
        }

        @Test
        void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic("chr1", "rs123456", Position.of(12345), Position.of(12345), "C", "C[2:321682[", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic or breakend alt allele C[2:321682["));
        }
    }

    @Nested
    public class ConvertBreakendTests {

        @Test
        void convertBreakend() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            Variant bnd = instance.convertBreakend("1", "bnd_U", Position.of(12345), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

            Breakend left = Breakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345));
            Breakend right = Breakend.of(b37.contigById(2), "bnd_V", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(321682));
            assertThat(bnd, equalTo(Variant.of("tra2", left, right, "C", "")));
        }

        @Test
        void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend("chr1", "rs123456", Position.of(12345), "C", "T", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele T"));
        }

        @Test
        void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend("chr1", "rs123456", Position.of(12345), "C", "<DEL>", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele <DEL>"));
        }
    }
}