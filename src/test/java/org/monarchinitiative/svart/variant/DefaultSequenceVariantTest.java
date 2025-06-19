package org.monarchinitiative.svart.variant;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.CoordinatesOutOfBoundsException;
import org.monarchinitiative.svart.Strand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class DefaultSequenceVariantTest {

    private final Contig chr1 = TestContig.of(1, 1000);

    @Test
    void telomericSequenceVariant() {
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 1001, 1002);
        GenomicVariant telomericVariant = GenomicVariant.of(chr1, "", Strand.POSITIVE, coordinates, "AT", "A");
        GenomicVariant oppositeStrand = telomericVariant.toOppositeStrand();
        assertThat(oppositeStrand.toOppositeStrand(), equalTo(telomericVariant));
    }

    @Test
    void throwsCoordinatesOutOfBoundsWithIncorrectCoordinates() {
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 1002, 1002);
        assertThrows(CoordinatesOutOfBoundsException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, coordinates, "A", "T"));
    }

    @Test
    void throwsIllegalArgumentWithIncorrectLength() {
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 1, 100);
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, coordinates, "A", "T"));
    }

    @Test
    void throwsIllegalArgumentWithSymbolicAllele() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "<INS>"));
    }

    @Test
    void throwsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "A[1:2]"));
    }

    @Test
    void shouldNotBeSymbolic() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    void shouldNotBeBreakend() {
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
        assertThat(instance.isBreakend(), equalTo(false));
    }

    @Test
    void snvOneBased() {
        GenomicVariant snv = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");

        assertThat(snv.start(), equalTo(snv.end()));
        assertThat(snv.variantType(), equalTo(VariantType.SNV));
        assertThat(snv.length(), equalTo(1));
        assertThat(snv.isOneBased(), equalTo(true));
    }

    @Test
    void snvZeroBased() {
        // BED / UCSC
        // https://www.genome.ucsc.edu/FAQ/FAQformat.html#format1
        // http://genome.ucsc.edu/blog/the-ucsc-genome-browser-coordinate-counting-systems/
        //
        // VCF is 1-based = [a, b] aka 'fully-closed' so for x in set [a, b] {x | a <= x <= b} (as is HGVS and GFF)
        // BED is 0-based = [a, b) aka 'half-open' so for x in set [a, b) {x | a <= x > b}
        //
        // NucleotideSeq:       A  T  G  C  T
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
        GenomicVariant snv = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T");

        assertThat(snv.start(), equalTo(0));
        assertThat(snv.end(), equalTo(1));
        assertThat(snv.variantType(), equalTo(VariantType.SNV));
        assertThat(snv.length(), equalTo(1));
        assertThat(snv.changeLength(), equalTo(0));
        assertThat(snv.isZeroBased(), equalTo(true));

        GenomicVariant snvStaticCons = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T");
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
    void delins() {
        GenomicVariant delins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AT", "TGC");

        assertThat(delins.start(), equalTo(1));
        assertThat(delins.start(), equalTo(1));
        assertThat(delins.end(), equalTo(2));
        assertThat(delins.variantType(), equalTo(VariantType.DELINS));
        assertThat(delins.length(), equalTo(2));
    }

    @Test
    void mnvZeroBased() {
        GenomicVariant mnv = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AT", "TG");

        assertThat(mnv.start(), equalTo(0));
        assertThat(mnv.start(), equalTo(0));
        assertThat(mnv.end(), equalTo(2));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    void del() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AG", "A");

        assertThat(del.start(), equalTo(1));
        assertThat(del.end(), equalTo(2));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
        assertThat(del.changeLength(), equalTo(-1));
    }

    @Test
    void delZeroBased() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AG", "A");

        assertThat(del.start(), equalTo(0));
        assertThat(del.end(), equalTo(2));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
        assertThat(del.changeLength(), equalTo(-1));
    }

    @Test
    void delZeroBasedTrimmedToEmpty() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "G", "");

        assertThat(del.start(), equalTo(1));
        assertThat(del.end(), equalTo(2));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(1));
        assertThat(del.changeLength(), equalTo(-1));
    }

    @Test
    void delOneBasedTrimmedToEmpty() {
        GenomicVariant del = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "G", "");

        assertThat(del.start(), equalTo(2));
        assertThat(del.end(), equalTo(2));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(1));
        assertThat(del.changeLength(), equalTo(-1));
    }

    @Test
    void ins() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");

        assertThat(ins.start(), equalTo(1));
        assertThat(ins.end(), equalTo(1));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    void insZeroBasedTrimmedToEmpty() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "", "G");

        assertThat(ins.start(), equalTo(1));
        assertThat(ins.end(), equalTo(1));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(0));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    void insOneBasedTrimmedToEmpty() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "", "G");

        assertThat(ins.start(), equalTo(2));
        assertThat(ins.end(), equalTo(1));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(0));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    void insZeroBased() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "AG");

        assertThat(ins.start(), equalTo(0));
        assertThat(ins.end(), equalTo(1));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    void insWithSameStrand() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void insWithNegativeStrand() {
        GenomicVariant ins = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "AG");
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
    void delWithNegativeStrand() {
        Contig chr5 = TestContig.of(5, 5);
        GenomicVariant del = GenomicVariant.of(chr5, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "AG", "A");
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
    void delZeroBasedWithNegativeStrand() {
        Contig chr5 = TestContig.of(5, 5);
        GenomicVariant del = GenomicVariant.of(chr5, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "AG", "A");
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
    void delLenSvLen() {
        GenomicVariant del = GenomicVariant.of(TestContigs.chr1, "rs2376870", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2827694, "CGTGGATGCGGGGAC", "C");
        //.    PASS   SVTYPE=DEL;LEN=15;HOMLEN=1;HOMSEQ=G;SVLEN=-14
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(15));
        assertThat(del.changeLength(), equalTo(-14));
    }

    @Test
    void snvToOppositeStrand() {
        GenomicVariant snv = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T");
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
    void DefaultVariantContainsSnv() {
        GenomicVariant largeIns = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "T", "<INS>", 100);
        assertTrue(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "T")));
        assertTrue(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "T")));
        assertFalse(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 200, "C", "A")));
        assertTrue(largeIns.contains(DefaultGenomicBreakend.of(chr1, "bnd_A", Strand.POSITIVE, CoordinateSystem.zeroBased(), 0, 0)));
    }

    @Test
    void DefaultVariantOverlapsOther() {
        GenomicVariant largeIns = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 100, "T", "<INS>", 100);
        GenomicVariant otherIns = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 99, 299, "C", "<INS>", 200);
        assertTrue(largeIns.overlapsWith(otherIns));
        assertTrue(otherIns.overlapsWith(largeIns));
    }

    @Test
    void throwsIllegalArgumentWithNonSymbolicAllele() {
        // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
        GenomicVariant instance = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "T", 0);
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
    void missingAllele() {
        Contig chr1 = TestContig.of(1, 5);
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "T", "*");
        GenomicVariant onNegative = variant.withStrand(Strand.NEGATIVE);
        assertThat(onNegative, equalTo(GenomicVariant.of(chr1, "", Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 5, "A", "*")));
    }

}