package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.CoordinatesOutOfBoundsException;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class DefaultSymbolicVariantTest {

    private final Contig chr1 = TestContig.of(1, 1000);

    @Test
    void throwsExceptionWhenNotSymbolic() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> DefaultSymbolicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "TAA", 2));
        assertThat(exception.getMessage(), containsString("Illegal non-symbolic alt allele 'TAA'"));
    }

    @Disabled("telomeric")
    @Test
    void telomericBreakend() {
        // chrUn_KI270435v1 92984 SV_318_2 N N[chr15:17081574[ 80
        DefaultSymbolicVariant variant = DefaultSymbolicVariant.of(GenomicAssemblies.GRCh38p13().contigByName("chrUn_KI270435v1"), Strand.POSITIVE, CoordinateSystem.ONE_BASED, 92984, 92984, "N", "N[chr15:17081574[", 0);
        System.out.println(variant.contig());
        System.out.println(variant);
    }

    @Test
    void shouldBeSymbolic() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
        assertThat(instance.isSymbolic(), equalTo(true));
    }

    @Test
    void shouldNotBeBreakend() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
        assertThat(instance.isBreakend(), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "A, <BND>",
            "A, <TRA>",
            "AGTC, GAGTC[2:8[",
    })
    void shouldBeBreakend(String ref, String alt) {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, ref, alt, 0);
        assertThat(instance.isBreakend(), equalTo(true));
    }

    @Test
    void throwsExceptionWhenOutOfContigBounds() {
        assertThrows(CoordinatesOutOfBoundsException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 2000, "A", "<DEL>", -99));
    }

    @Test
    void symbolicDel() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);

        assertThat(del.contig(), equalTo(chr1));
        assertThat(del.start(), equalTo(1));
        assertThat(del.end(), equalTo(100));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(100));
        assertThat(del.ref(), equalTo("A"));
        assertThat(del.alt(), equalTo("<DEL>"));
        assertThat(del.changeLength(), equalTo(-99));

        // END: End reference position (1-based), indicating the variant spans positions POS-END on reference/contig CHROM.
        //   END is deprecated and has been replaced with LEN.
        // LEN: The length of the variant's alignment to the reference indicating the variant spans positions POS-(LEN - 1) on reference/contig CHROM.
        //    LEN = length of REF allele
        // changeLength == SVLEN
        // SVLEN Difference in length between REF and ALT alleles
        //  (longer ALT (insertions) have positive values, shorter ALT alleles (e.g. deletions) have negative values
    }

    @Test
    void symbolicDelZeroBased() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 100, "A", "<DEL>", -99);

        assertThat(del.contig(), equalTo(chr1));
        assertThat(del.start(), equalTo(0));
        assertThat(del.end(), equalTo(100));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(100));
        assertThat(del.ref(), equalTo("A"));
        assertThat(del.alt(), equalTo("<DEL>"));
        assertThat(del.changeLength(), equalTo(-99));

        // END: End reference position (1-based), indicating the variant spans positions POS-END on reference/contig CHROM.
        //   END is deprecated and has been replaced with LEN.
        // LEN: The length of the variant's alignment to the reference indicating the variant spans positions POS-(LEN - 1) on reference/contig CHROM.
        //    LEN = length of REF allele
        // changeLength == SVLEN
        // SVLEN Difference in length between REF and ALT alleles
        //  (longer ALT (insertions) have positive values, shorter ALT alleles (e.g. deletions) have negative values
    }

    @Test
    void symbolicDelLenSvLen() {
        //1       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
        GenomicVariant del = GenomicVariant.of(TestContigs.chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321682 + 205, "T", "<DEL>", -205);
        assertThat(del.length(), equalTo(206));
        assertThat(del.changeLength(), equalTo(-205));
    }

    @Test
    void symbolicIns() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);

        assertThat(ins.contig(), equalTo(chr1));
        assertThat(ins.start(), equalTo(1));
        assertThat(ins.end(), equalTo(1));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.ref(), equalTo("A"));
        assertThat(ins.alt(), equalTo("<INS>"));
        assertThat(ins.changeLength(), equalTo(100));
    }

    @Test
    void symbolicInsWithSameStrand() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void symbolicInsWithNegativeStrand() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
        GenomicVariant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.contig(), equalTo(chr1));
        assertThat(negativeIns.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.start(), equalTo(1000));
        assertThat(negativeIns.end(), equalTo(1000));
        assertThat(negativeIns.ref(), equalTo("T"));
        assertThat(negativeIns.alt(), equalTo("<INS>"));
        assertThat(negativeIns.variantType(), equalTo(VariantType.INS));
        assertThat(negativeIns.length(), equalTo(1));
        assertThat(negativeIns.changeLength(), equalTo(100));
    }

    @Test
    void symbolicDelWithNegativeStrand() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);
        GenomicVariant negative = instance.withStrand(Strand.NEGATIVE);

        assertThat(negative.contig(), equalTo(chr1));
        assertThat(negative.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negative.start(), equalTo(901));
        assertThat(negative.end(), equalTo(1000));
        assertThat(negative.ref(), equalTo("T"));
        assertThat(negative.alt(), equalTo("<DEL>"));
        assertThat(negative.variantType(), equalTo(VariantType.DEL));
        assertThat(negative.length(), equalTo(100));
        assertThat(negative.changeLength(), equalTo(-99));
    }

    @Test
    void symbolicDelZeroBasedWithNegativeStrand() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 100, "A", "<DEL>", -99);
        GenomicVariant negative = instance.withStrand(Strand.NEGATIVE);

        assertThat(negative.contig(), equalTo(chr1));
        assertThat(negative.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negative.start(), equalTo(900));
        assertThat(negative.end(), equalTo(1000));
        assertThat(negative.ref(), equalTo("T"));
        assertThat(negative.alt(), equalTo("<DEL>"));
        assertThat(negative.variantType(), equalTo(VariantType.DEL));
        assertThat(negative.length(), equalTo(100));
        assertThat(negative.changeLength(), equalTo(-99));
    }

    @Test
    void compareWithGenomicRegion() {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 10);
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "A", "TAA");
        assertThat(variant.isSymbolic(), is(false));
        assertThat(GenomicRegion.compare(region, variant), equalTo(-1));
        assertThat(region.contains(variant), equalTo(true));
    }

    @Test
    void canCreateSymbolicBnd() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0);
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("<BND>"));
        assertThat(instance.variantType(), equalTo(VariantType.BND));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.changeLength(), equalTo(0));
    }

    @Test
    void canCreateSymbolicBreakendAllele() {
        GenomicVariant instance = DefaultSymbolicVariant.of(chr1, "bnd_u", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "G]2:123456]", 0, "bnd_v", "event_1");
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("G]2:123456]"));
        assertThat(instance.variantType(), equalTo(VariantType.BND));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.changeLength(), equalTo(0));
        assertThat(instance.mateId(), equalTo("bnd_v"));
        assertThat(instance.eventId(), equalTo("event_1"));
    }

    @Test
    void isSymbolic() {
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").isSymbolic(), is(false));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99).isSymbolic(), is(true));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0).isSymbolic(), is(true));
        assertThat(GenomicVariant.of(chr1, "bnd_u", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "G]2:123456]", 0).isSymbolic(), is(true));
    }

    @Test
    void comparableTest() {
        GenomicVariant first = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0);
        GenomicVariant second = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant third = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);
        Contig chr2 = TestContig.of(2, 2000);
        GenomicVariant fourth = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");

        List<GenomicVariant> sorted = Stream.of(second, first, fourth, third).sorted().toList();
        assertThat(sorted, equalTo(List.of(first, second, third, fourth)));
    }
}