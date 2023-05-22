package org.monarchinitiative.svart.impl;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultGenomicVariantTest {
    private final Contig chr1 = TestContig.of(1, 1000);

    @Nested
    public class NonSymbolicTests {

        @Test
        public void throwsIllegalArgumentWithIncorrectLength() {
            Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 1, 100);
            assertThrows(IllegalArgumentException.class, () -> DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, coordinates, "A", "T"));
        }

        @Test
        public void throwsIllegalArgumentWithSymbolicAllele() {
            assertThrows(IllegalArgumentException.class, () -> DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "<INS>"));
        }

        @Test
        public void throwsIllegalArgumentWithBreakendAllele() {
            assertThrows(IllegalArgumentException.class, () -> DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "A[1:2]"));
        }

        @Test
        public void shouldNotBeSymbolic() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
            assertThat(instance.isSymbolic(), equalTo(false));
        }

        @Test
        public void shouldNotBeBreakend() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
            assertThat(instance.isBreakend(), equalTo(false));
        }

        @Test
        public void snvOneBased() {
            GenomicVariant snv = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");

            assertThat(snv.start(), equalTo(snv.end()));
            assertThat(snv.variantType(), equalTo(VariantType.SNV));
            assertThat(snv.length(), equalTo(1));
            assertThat(snv.isOneBased(), equalTo(true));
        }

        @Test
        public void snvZeroBased() {
            // BED / UCSC
            // https://www.genome.ucsc.edu/FAQ/FAQformat.html#format1
            // http://genome.ucsc.edu/blog/the-ucsc-genome-browser-coordinate-counting-systems/
            //
            // VCF is 1-based = [a, b] aka 'fully-closed' so for x in set [a, b] {x | a <= x <= b} (as is HGVS and GFF)
            // BED is 0-based = [a, b) aka 'half-open' so for x in set [a, b) {x | a <= x > b}
            //
            // Seq:       A  T  G  C  T
            // 1-based:   1  2  3  4  5
            // 0-based: 0  1  2  3  4  5
            //
            // 1-based AT = 1, 2   len = (2 - 1) + 1
            // 0-based AT = 0, 2   len = 2 - 0
            //
            // Given the end position is numerically identical in both coordinate systems, it might make more sense to define
            // the coordinate system directly rather than messing about with Positions.

            // e.g. OneBasedInterval.of(1, 1).toZeroBased().equalTo(ZeroBasedInterval.of(0, 1)). This is sort of what the
            // GenomicRegion is trying to do.
            //
            // Joda time uses half-open intervals - https://www.joda.org/joda-time/key_interval.html
            // "Intervals are implemented as half-open, which is to say that the start instant is inclusive but the end instant is exclusive.
            // The end is always greater than or equal to the start. The interval is also restricted to just one chronology and time zone."
            //
            // Java time https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html
            // the Duration class is closest to Interval https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html and has a 'between' method
            //  public static Duration between(Temporal startInclusive, Temporal endExclusive) specifies '0-based' coordinates


            // e.g. this is from a BED file which uses 0-based coordinates - the endPosition is 1, but it doesn't really have
            // a coordinate system itself as these values are identical in both systems. So really it's the _interval_ which has
            // the coordinate system, not the positions.
            // In this case if both positions are zero-based everything fails.
            GenomicVariant snv = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T");

            assertThat(snv.start(), equalTo(0));
            assertThat(snv.end(), equalTo(1));
            assertThat(snv.variantType(), equalTo(VariantType.SNV));
            assertThat(snv.length(), equalTo(1));
            assertThat(snv.changeLength(), equalTo(0));
            assertThat(snv.isZeroBased(), equalTo(true));

            GenomicVariant snvStaticCons = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T");
            assertThat(snvStaticCons.start(), equalTo(0));
            assertThat(snvStaticCons.end(), equalTo(1));
            assertThat(snvStaticCons.variantType(), equalTo(VariantType.SNV));
            assertThat(snvStaticCons.length(), equalTo(1));
            assertThat(snvStaticCons.changeLength(), equalTo(0));
            assertThat(snvStaticCons.isZeroBased(), equalTo(true));

            MatcherAssert.assertThat(GenomicVariant.compare(snvStaticCons, snv), equalTo(0));
            assertThat(snvStaticCons, equalTo(snv));
        }

        @Test
        public void delins() {
            GenomicVariant delins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AT", "TGC");

            assertThat(delins.start(), equalTo(1));
            assertThat(delins.start(), equalTo(1));
            assertThat(delins.end(), equalTo(2));
            assertThat(delins.variantType(), equalTo(VariantType.DELINS));
            assertThat(delins.length(), equalTo(2));
        }

        @Test
        public void mnvZeroBased() {
            GenomicVariant mnv = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AT", "TG");

            assertThat(mnv.start(), equalTo(0));
            assertThat(mnv.start(), equalTo(0));
            assertThat(mnv.end(), equalTo(2));
            assertThat(mnv.variantType(), equalTo(VariantType.MNV));
            assertThat(mnv.length(), equalTo(2));
        }

        @Test
        public void del() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AG", "A");

            assertThat(del.start(), equalTo(1));
            assertThat(del.end(), equalTo(2));
            assertThat(del.variantType(), equalTo(VariantType.DEL));
            assertThat(del.length(), equalTo(2));
            assertThat(del.changeLength(), equalTo(-1));
        }

        @Test
        public void delZeroBased() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AG", "A");

            assertThat(del.start(), equalTo(0));
            assertThat(del.end(), equalTo(2));
            assertThat(del.variantType(), equalTo(VariantType.DEL));
            assertThat(del.length(), equalTo(2));
            assertThat(del.changeLength(), equalTo(-1));
        }

        @Test
        public void delZeroBasedTrimmedToEmpty() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "G", "");

            assertThat(del.start(), equalTo(1));
            assertThat(del.end(), equalTo(2));
            assertThat(del.variantType(), equalTo(VariantType.DEL));
            assertThat(del.length(), equalTo(1));
            assertThat(del.changeLength(), equalTo(-1));
        }

        @Test
        public void delOneBasedTrimmedToEmpty() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "G", "");

            assertThat(del.start(), equalTo(2));
            assertThat(del.end(), equalTo(2));
            assertThat(del.variantType(), equalTo(VariantType.DEL));
            assertThat(del.length(), equalTo(1));
            assertThat(del.changeLength(), equalTo(-1));
        }

        @Test
        public void ins() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");

            assertThat(ins.start(), equalTo(1));
            assertThat(ins.end(), equalTo(1));
            assertThat(ins.variantType(), equalTo(VariantType.INS));
            assertThat(ins.length(), equalTo(1));
            assertThat(ins.changeLength(), equalTo(1));
        }

        @Test
        public void insZeroBasedTrimmedToEmpty() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "", "G");

            assertThat(ins.start(), equalTo(1));
            assertThat(ins.end(), equalTo(1));
            assertThat(ins.variantType(), equalTo(VariantType.INS));
            assertThat(ins.length(), equalTo(0));
            assertThat(ins.changeLength(), equalTo(1));
        }

        @Test
        public void insOneBasedTrimmedToEmpty() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "", "G");

            assertThat(ins.start(), equalTo(2));
            assertThat(ins.end(), equalTo(1));
            assertThat(ins.variantType(), equalTo(VariantType.INS));
            assertThat(ins.length(), equalTo(0));
            assertThat(ins.changeLength(), equalTo(1));
        }

        @Test
        public void insZeroBased() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "AG");

            assertThat(ins.start(), equalTo(0));
            assertThat(ins.end(), equalTo(1));
            assertThat(ins.variantType(), equalTo(VariantType.INS));
            assertThat(ins.length(), equalTo(1));
            assertThat(ins.changeLength(), equalTo(1));
        }

        @Test
        public void insWithSameStrand() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");
            assertSame(ins, ins.withStrand(Strand.POSITIVE));
        }

        @Test
        public void insWithNegativeStrand() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");
            GenomicVariant negativeIns = ins.withStrand(Strand.NEGATIVE);

            assertThat(negativeIns.contig(), equalTo(chr1));
            assertThat(negativeIns.strand(), equalTo(Strand.NEGATIVE));
            assertThat(negativeIns.start(), equalTo(1000));
            assertThat(negativeIns.end(), equalTo(1000));
            assertThat(negativeIns.ref(), equalTo("T"));
            assertThat(negativeIns.alt(), equalTo("CT"));
            assertThat(negativeIns.variantType(), equalTo(VariantType.INS));
            assertThat(negativeIns.length(), equalTo(1));
        }

        @Test
        public void delWithNegativeStrand() {
            Contig chr5 = TestContig.of(5, 5);
            GenomicVariant del = DefaultGenomicVariant.of(chr5, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AG", "A");
            GenomicVariant negativeDel = del.withStrand(Strand.NEGATIVE);

            assertThat(negativeDel.contig(), equalTo(chr5));
            assertThat(negativeDel.strand(), equalTo(Strand.NEGATIVE));
            assertThat(negativeDel.start(), equalTo(4));
            assertThat(negativeDel.end(), equalTo(5));
            assertThat(negativeDel.ref(), equalTo("CT"));
            assertThat(negativeDel.alt(), equalTo("T"));
            assertThat(negativeDel.variantType(), equalTo(VariantType.DEL));
            assertThat(negativeDel.length(), equalTo(2));
            assertThat(negativeDel.changeLength(), equalTo(-1));
        }

        @Test
        public void delZeroBasedWithNegativeStrand() {
            Contig chr5 = TestContig.of(5, 5);
            GenomicVariant del = DefaultGenomicVariant.of(chr5, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AG", "A");
            GenomicVariant negativeDel = del.withStrand(Strand.NEGATIVE);

            assertThat(negativeDel.contig(), equalTo(chr5));
            assertThat(negativeDel.strand(), equalTo(Strand.NEGATIVE));
            assertThat(negativeDel.start(), equalTo(3));
            assertThat(negativeDel.end(), equalTo(5));
            assertThat(negativeDel.ref(), equalTo("CT"));
            assertThat(negativeDel.alt(), equalTo("T"));
            assertThat(negativeDel.variantType(), equalTo(VariantType.DEL));
            assertThat(negativeDel.length(), equalTo(2));
            assertThat(negativeDel.changeLength(), equalTo(-1));
        }

        @Test
        public void delLenSvLen() {
            GenomicVariant del = DefaultGenomicVariant.of(TestContigs.chr1, "rs2376870", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2827694, "CGTGGATGCGGGGAC", "C");
            //.    PASS   SVTYPE=DEL;LEN=15;HOMLEN=1;HOMSEQ=G;SVLEN=-14
            assertThat(del.variantType(), equalTo(VariantType.DEL));
            assertThat(del.length(), equalTo(15));
            assertThat(del.changeLength(), equalTo(-14));
        }

        @Test
        public void snvToOppositeStrand() {
            GenomicVariant snv = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
            assertThat(snv.strand(), equalTo(Strand.POSITIVE));
            GenomicVariant oppositeSnv = snv.toOppositeStrand();
            assertThat(oppositeSnv.contig(), equalTo(chr1));
            assertThat(oppositeSnv.strand(), equalTo(Strand.NEGATIVE));
            assertThat(oppositeSnv.start(), equalTo(1000));
            assertThat(oppositeSnv.end(), equalTo(1000));
            assertThat(oppositeSnv.ref(), equalTo("T"));
            assertThat(oppositeSnv.alt(), equalTo("A"));
            assertThat(oppositeSnv.variantType(), equalTo(VariantType.SNV));
            assertThat(oppositeSnv.length(), equalTo(1));
            assertThat(oppositeSnv.changeLength(), equalTo(0));
        }

        @Test
        public void DefaultVariantContainsSnv() {
            GenomicVariant largeIns = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "T", "<INS>", 100);
            assertTrue(largeIns.contains(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T")));
            assertTrue(largeIns.contains(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T")));
            assertFalse(largeIns.contains(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 200, "C", "A")));
            assertTrue(largeIns.contains(DefaultGenomicBreakend.of(chr1, "bnd_A", Strand.POSITIVE, CoordinateSystem.zeroBased(), 0, 0)));
        }

        @Test
        public void DefaultVariantOverlapsOther() {
            GenomicVariant largeIns = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "T", "<INS>", 100);
            GenomicVariant otherIns = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 99, 299, "C", "<INS>", 200);
            assertTrue(largeIns.overlapsWith(otherIns));
            assertTrue(otherIns.overlapsWith(largeIns));
        }

        @Test
        public void throwsIllegalArgumentWithNonSymbolicAllele() {
            // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "T", 0);
            assertThat(instance.contig(), equalTo(chr1));
            assertThat(instance.start(), equalTo(1));
            assertThat(instance.end(), equalTo(1));
            assertThat(instance.variantType(), equalTo(VariantType.SNV));
            assertThat(instance.length(), equalTo(1));
            assertThat(instance.ref(), equalTo("A"));
            assertThat(instance.alt(), equalTo("T"));
            assertThat(instance.changeLength(), equalTo(0));
        }

        @Test
        public void missingAllele() {
            Contig chr1 = TestContig.of(1, 5);
            GenomicVariant variant = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "T", "*");
            GenomicVariant onNegative = variant.withStrand(Strand.NEGATIVE);
            assertThat(onNegative, equalTo(DefaultGenomicVariant.of(chr1, "", Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 5, "A", "*")));
        }

    }

    @Nested
    public class SymbolicGenomicVariantTests {
        @Test
        public void shouldBeSymbolic() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
            assertThat(instance.isSymbolic(), equalTo(true));
        }

        @Test
        public void shouldNotBeBreakend() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
            assertThat(instance.isBreakend(), equalTo(false));
        }

        @Test
        public void shouldBeBreakend() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 100);
            assertThat(instance.isBreakend(), equalTo(true));
        }

        @Test
        public void symbolicDel() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);

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
        public void symbolicDelZeroBased() {
            GenomicVariant del = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 100, "A", "<DEL>", -99);

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
        public void symbolicDelLenSvLen() {
            //1       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
            GenomicVariant del = DefaultGenomicVariant.of(TestContigs.chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321682 + 205, "T", "<DEL>", -205);
            assertThat(del.length(), equalTo(206));
            assertThat(del.changeLength(), equalTo(-205));
        }

        @Test
        public void symbolicIns() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);

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
        public void symbolicInsWithSameStrand() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
            assertSame(ins, ins.withStrand(Strand.POSITIVE));
        }

        @Test
        public void symbolicInsWithNegativeStrand() {
            GenomicVariant ins = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 100);
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
        public void symbolicDelWithNegativeStrand() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);
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
        public void symbolicDelZeroBasedWithNegativeStrand() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 100, "A", "<DEL>", -99);
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
        public void compareWithGenomicRegion() {
            GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 10);
            GenomicVariant variant = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "A", "TAA");
            assertThat(variant.isSymbolic(), is(false));
            assertThat(GenomicRegion.compare(region, variant), equalTo(-1));
            assertThat(region.contains(variant), equalTo(true));
        }

        @Test
        public void canCreateSymbolicBnd() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0);
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
        public void canCreateSymbolicBreakendAllele() {
            GenomicVariant instance = DefaultGenomicVariant.of(chr1, "bnd_u", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "G]2:123456]", 0, "bnd_v", "event_1");
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
        public void isSymbolic() {
            assertThat(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").isSymbolic(), is(false));
            assertThat(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99).isSymbolic(), is(true));
            assertThat(DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0).isSymbolic(), is(true));
            assertThat(DefaultGenomicVariant.of(chr1, "bnd_u", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "G]2:123456]", 0).isSymbolic(), is(true));
        }

        @Test
        public void comparableTest() {
            GenomicVariant first = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<BND>", 0);
            GenomicVariant second = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
            GenomicVariant third = DefaultGenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "A", "<DEL>", -99);
            Contig chr2 = TestContig.of(2, 2000);
            GenomicVariant fourth = DefaultGenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");

            List<GenomicVariant> sorted = Stream.of(second, first, fourth, third).sorted().toList();
            assertThat(sorted, equalTo(List.of(first, second, third, fourth)));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "<>, A, ref",
            "A, WIBBLE>, alt",
            "N, <WIBBLE, alt",
            "., AT, ref",
            "*, AT, ref",
            "., ., ref",
            "*, *, ref",
            "<INS>, AT, ref",
            "A, ART, alt",
            "A, 'A TT', alt",
            "'', W, alt",
            "a, R, alt",
            "R, a, ref",
            "A, ?, alt",
            "?, ?, ref", // both illegal, but ref is checked first
    })
    void nonsenseInputTests(String ref, String alt, Allele illegalAllele) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 2, ref, alt, 2));
        assertThat(exception.getMessage(), equalTo("Illegal " + illegalAllele + " allele: " + (illegalAllele == Allele.ref? ref : alt)));
    }

    private enum Allele {
        ref, alt
    }
}