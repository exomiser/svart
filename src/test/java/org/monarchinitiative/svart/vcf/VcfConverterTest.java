package org.monarchinitiative.svart.vcf;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.ConfidenceInterval;
import org.monarchinitiative.svart.variant.*;
import org.monarchinitiative.svart.sequence.VariantTrimmer;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VcfConverterTest {

    private final GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
    private final Contig chr1 = b37.contigById(1);
    private final Contig chr2 = b37.contigById(2);

    private final VcfConverter instance = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

    static class TestAbstractVariant extends AbstractGenomicVariant<TestAbstractVariant> {

        protected TestAbstractVariant(GenomicVariant genomicVariant) {
            super(genomicVariant);
        }

        TestAbstractVariant(AbstractGenomicVariant.Builder<?> builder) {
            super(builder);
        }

        /**
         * @param genomicVariant
         * @return
         */
        @Override
        protected TestAbstractVariant newVariantInstance(GenomicVariant genomicVariant) {
            return new TestAbstractVariant(genomicVariant);
        }


        static Builder builder() {
            return new Builder();
        }

        static class Builder extends AbstractGenomicVariant.Builder<TestAbstractVariant.Builder> {

            public TestAbstractVariant build() {
                return new TestAbstractVariant(this);
            }

            @Override
            protected TestAbstractVariant.Builder self() {
                return this;
            }
        }
    }

    @Nested
    class ConvertNonSymbolicTests {

        @Test
        void convert() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            GenomicVariant snv = instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "T");
            assertThat(snv, equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        void convertWithBuilder() {
            TestAbstractVariant.Builder builder = instance.convert(TestAbstractVariant.builder(), instance.parseContig("chr1"), "rs123456", 12345, "C", "T");
            TestAbstractVariant variant = builder.build();
            assertThat(variant.genomicVariant(), equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        void trimsInput() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	C	T	6	PASS	.
            GenomicVariant snv = instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "CCC", "TCC");
            assertThat(snv, equalTo(GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));
        }

        @Test
        void throwsExceptionWithMultiAllelicSite() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	rs123456	CCC	TC, TCC	6	PASS	.
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "CCC", "TC, TCC"));
            assertThat(exception.getMessage(), equalTo("Illegal multi-allelic alt allele 'TC, TCC'"));
        }

        @Test
        void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "<DEL>"));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele '<DEL>'"));
        }

        @Test
        void throwsExceptionWithBreakendAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convert(instance.parseContig("chr1"), "rs123456", 12345, "C", "C[2:321682["));
            assertThat(exception.getMessage(), equalTo("Illegal symbolic alt allele 'C[2:321682['"));
        }
    }

    @Nested
    class ConvertSymbolicTests {

        @Test
        void convertSymbolic() {
            // CHR	POS	ID	REF	ALT
            // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
            GenomicVariant ins = instance.convertSymbolic(instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
            assertThat(ins, equalTo(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, 12345, "C", "<INS>", 200)));
        }

        @Test
        void convertSymbolicWithBuilder() {
            TestAbstractVariant.Builder builder = instance.convertSymbolic(TestAbstractVariant.builder(), instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
            TestAbstractVariant variant = builder.build();
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
        void convertSymbolicWithFullyTrimmedRefAlleleBuilder() {
            VcfConverter converter = new VcfConverter(b37, VariantTrimmer.rightShiftingTrimmer(VariantTrimmer.removingCommonBase()));
            TestAbstractVariant.Builder builder = converter.convertSymbolic(TestAbstractVariant.builder(), instance.parseContig("chr1"), "", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "<INS>", 200);
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
        void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertSymbolic(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), 12345, ConfidenceInterval.precise(), "C", "T", 0));
            assertThat(exception.getMessage(), equalTo("Illegal non-symbolic alt allele 'T'"));
        }

        @Test
        void convertSymbolicWithBreakendAllele() {
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
    class ConvertGenomicBreakendTests {

        @Test
        void convertBreakend() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            GenomicVariant bnd = instance.convertBreakend(instance.parseContig("chr1"), "bnd_U", 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

            GenomicBreakend left = GenomicBreakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12346, 12345);
            GenomicBreakend right = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321681);
            assertThat(bnd, equalTo(GenomicBreakendVariant.of("tra2", left, right, "C", "")));
        }

        @Test
        void convertBreakendWithBuilder() {
            // CHR	POS	ID	REF	ALT
            // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
            DefaultGenomicBreakendVariant.Builder builder = instance.convertBreakend(DefaultGenomicBreakendVariant.builder(), instance.parseContig("1"), "bnd_U", 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");
            GenomicVariant bnd = builder.build();
            GenomicBreakend left = GenomicBreakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12346, 12345);
            GenomicBreakend right = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321681);
            assertThat(bnd, equalTo(GenomicBreakendVariant.of("tra2", left, right, "C", "")));
        }

        @Test
        void throwsExceptionWithNonSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), "C", "T", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele 'T'"));
        }

        @Test
        void throwsExceptionWithSymbolicAllele() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.convertBreakend(instance.parseContig("chr1"), "rs123456", 12345, ConfidenceInterval.precise(), "C", "<DEL>", ConfidenceInterval.precise(), "", ""));
            assertThat(exception.getMessage(), equalTo("Illegal non-breakend alt allele '<DEL>'"));
        }
    }
}