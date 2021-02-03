package org.monarchinitiative.svart.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.impl.DefaultBreakendVariant;
import org.monarchinitiative.svart.impl.DefaultVariant;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VcfConverterTest {

    private final GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
    private final Contig chr1 = b37.contigById(1);
    private final Contig chr2 = b37.contigById(2);

    private final VcfConverter instance = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

    @Nested
    public class ConvertNonSymbolicTests {

        @Test
        public void convert() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            Variant snv = instance.convert("chr1", "rs123456", 12345, "C", "T");
            assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));
        }

        @Test
        public void convertWithBuilder() {
            DefaultVariant.Builder builder = instance.convert(DefaultVariant.builder(), "chr1", "rs123456", 12345, "C", "T");
            Variant variant = builder.build();
            assertThat(variant, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));
        }

        @Test
        public void trimsInput() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            Variant snv = instance.convert("chr1", "rs123456", 12345, "CCC", "TCC");
            assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));
        }

        @Test
        public void throwsExceptionWithMultiAllelicSite() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	CCC	TC, TCC	6	PASS	.
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "CCC", "TC, TCC"));
            assertThat(exception.getMessage(), equalTo("Illegal multi-allelic alt allele TC, TCC"));
        }

        @Test
        public void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "C", "<DEL>"));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele <DEL>"));
        }

        @Test
        public void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert("chr1", "rs123456", 12345, "C", "C[2:321682["));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele C[2:321682["));
        }
    }

    @Nested
    public class ConvertSymbolicTests {

        @Test
        public void convertSymbolic() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
            Variant ins = instance.convertSymbolic("chr1", "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
            assertThat(ins, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), Position.of(12345), "C", "<INS>", 200)));
        }

        @Test
        public void convertSymbolicWithBuilder() {
            TestVariant.Builder builder = instance.convertSymbolic(TestVariant.builder(), "chr1", "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
            Variant variant = builder.build();
            assertThat(variant.isSymbolic(), equalTo(true));
            assertThat(variant.contig(), equalTo(chr1));
            assertThat(variant.id(), equalTo(""));
            assertThat(variant.start(), equalTo(12345));
            assertThat(variant.end(), equalTo(12345));
            assertThat(variant.ref(), equalTo("C"));
            assertThat(variant.alt(), equalTo("<INS>"));
            assertThat(variant.length(), equalTo(1));
            assertThat(variant.changeLength(), equalTo(200));
        }

        @Test
        public void convertSymbolicWithFullyTrimmedRefAlleleBuilder() {
            VcfConverter converter = new VcfConverter(b37, VariantTrimmer.rightShiftingTrimmer(VariantTrimmer.removingCommonBase()));
            TestVariant.Builder builder = converter.convertSymbolic(TestVariant.builder(), "chr1", "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
            Variant variant = builder.build();
            assertThat(variant.isSymbolic(), equalTo(true));
            assertThat(variant.contig(), equalTo(chr1));
            assertThat(variant.id(), equalTo(""));
            assertThat(variant.start(), equalTo(12346));
            assertThat(variant.end(), equalTo(12345));
            assertThat(variant.ref(), equalTo(""));
            assertThat(variant.alt(), equalTo("<INS>"));
            assertThat(variant.length(), equalTo(0));
            assertThat(variant.changeLength(), equalTo(200));
        }

        @Test
        public void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic("chr1", "rs123456", Position.of(12345), Position.of(12345), "C", "T", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic or breakend alt allele T"));
        }

        @Test
        public void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic("chr1", "rs123456", Position.of(12345), Position.of(12345), "C", "C[2:321682[", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic or breakend alt allele C[2:321682["));
        }
    }

    @Nested
    public class ConvertBreakendTests {

        @Test
        public void convertBreakend() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            Variant bnd = instance.convertBreakend("1", "bnd_U", Position.of(12345), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

            Breakend left = Breakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12346), Position.of(12345));
            Breakend right = Breakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(321682), Position.of(321681));
            assertThat(bnd, equalTo(Variant.of("tra2", left, right, "C", "")));
        }

        @Test
        public void convertBreakendWithBuilder() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            DefaultBreakendVariant.Builder builder = instance.convertBreakend(DefaultBreakendVariant.builder(), "1", "bnd_U", Position.of(12345), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");
            Variant bnd = builder.build();
            Breakend left = Breakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12346), Position.of(12345));
            Breakend right = Breakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(321682), Position.of(321681));
            assertThat(bnd, equalTo(Variant.of("tra2", left, right, "C", "")));
        }

        @Test
        public void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend("chr1", "rs123456", Position.of(12345), "C", "T", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele T"));
        }

        @Test
        public void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend("chr1", "rs123456", Position.of(12345), "C", "<DEL>", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele <DEL>"));
        }
    }
}