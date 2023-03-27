package org.monarchinitiative.svart.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.impl.DefaultGenomicBreakendVariant;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertSame;
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
            GenomicVariant snv = instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "T");
            assertThat(snv, equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        public void convertWithBuilder() {
            GenomicVariant.Builder builder = instance.convert(GenomicVariant.builder(), instance.parseContig("chr1"), "rs123456", 12345, "C", "T");
            GenomicVariant variant = builder.build();
            assertThat(variant, equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        public void trimsInput() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            GenomicVariant snv = instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "CCC", "TCC");
            assertThat(snv, equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        public void throwsExceptionWithMultiAllelicSite() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	CCC	TC, TCC	6	PASS	.
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "CCC", "TC, TCC"));
            assertThat(exception.getMessage(), equalTo("Illegal multi-allelic alt allele 'TC, TCC'"));
        }

        @Test
        public void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "<DEL>"));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele '<DEL>'"));
        }

        @Test
        public void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "C[2:321682["));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele 'C[2:321682['"));
        }
    }

    @Nested
    public class ConvertSymbolicTests {

        @Test
        public void convertSymbolic() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
            GenomicVariant ins = instance.convertSymbolic(instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
            assertThat(ins, equalTo(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, 12345, "C", "<INS>", 200)));
        }

        @Test
        public void convertSymbolicWithBuilder() {
            GenomicVariant.Builder builder = instance.convertSymbolic(GenomicVariant.builder(), instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
            GenomicVariant variant = builder.build();
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
            GenomicVariant.Builder builder = converter.convertSymbolic(GenomicVariant.builder(), instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
            GenomicVariant variant = builder.build();
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
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "T", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic alt allele 'T'"));
        }

        @Test
        public void convertSymbolicWithBreakendAllele() {
            GenomicVariant bnd = instance.convertSymbolic(instance.parseContig("chr1"), "bnd_U", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", 0, "bnd_V", "tra2");
            assertThat(bnd.isSymbolic(), equalTo(true));
            assertThat(bnd.isBreakend(), equalTo(true));
            assertThat(bnd.ref(), equalTo("C"));
            assertThat(bnd.alt(), equalTo("C[2:321682["));
            // breakends cannot be flipped from one strand to the other
            assertSame(bnd, bnd.toOppositeStrand());
        }
    }

    @Nested
    public class ConvertGenomicBreakendTests {

        @Test
        public void convertBreakend() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            GenomicVariant bnd = instance.convertBreakend(instance.parseContig("chr1"), "bnd_U", 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

            GenomicBreakend left = GenomicBreakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12346, 12345);
            GenomicBreakend right = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321681);
            assertThat(bnd, equalTo(GenomicVariant.of("tra2", left, right, "C", "")));
        }

        @Test
        public void convertBreakendWithBuilder() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            DefaultGenomicBreakendVariant.Builder builder = instance.convertBreakend(DefaultGenomicBreakendVariant.builder(), instance.parseContig("1"), "bnd_U", 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");
            GenomicVariant bnd = builder.build();
            GenomicBreakend left = GenomicBreakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12346, 12345);
            GenomicBreakend right = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321681);
            assertThat(bnd, equalTo(GenomicVariant.of("tra2", left, right, "C", "")));
        }

        @Test
        public void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), "C", "T", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele 'T'"));
        }

        @Test
        public void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), "C", "<DEL>", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele '<DEL>'"));
        }
    }
}