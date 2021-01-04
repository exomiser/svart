package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultVariantTest {
    private final Contig chr1 = TestContig.of(1, 1000);

    @Test
    public void throwsIllegalArgumentWithSymbolicAllele() {
        assertThrows(IllegalArgumentException.class, () -> DefaultVariant.oneBased(chr1, 1, "A", "<INS>"));
    }

    @Test
    public void throwsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> DefaultVariant.oneBased(chr1, 1, "A", "A[1:2]"));
    }

    @Test
    public void shouldNotBeSymbolic() {
        Variant instance = DefaultVariant.oneBased(chr1, 1, "A", "T");
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    public void snvOneBased() {
        Variant snv = DefaultVariant.oneBased(chr1, 1, "A", "T");

        assertThat(snv.startPosition(), equalTo(snv.endPosition()));
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
        Variant snv = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(0), "A", "T");

        assertThat(snv.startPosition(), equalTo(Position.of(0)));
        assertThat(snv.endPosition(), equalTo(Position.of(1)));
        assertThat(snv.variantType(), equalTo(VariantType.SNV));
        assertThat(snv.length(), equalTo(1));
        assertThat(snv.refLength(), equalTo(1));
        assertThat(snv.changeLength(), equalTo(0));
        assertThat(snv.isZeroBased(), equalTo(true));

        Variant snvStaticCons = DefaultVariant.zeroBased(chr1, 0, "A", "T");
        assertThat(snvStaticCons.startPosition(), equalTo(Position.of(0)));
        assertThat(snvStaticCons.endPosition(), equalTo(Position.of(1)));
        assertThat(snvStaticCons.variantType(), equalTo(VariantType.SNV));
        assertThat(snvStaticCons.length(), equalTo(1));
        assertThat(snvStaticCons.refLength(), equalTo(1));
        assertThat(snvStaticCons.changeLength(), equalTo(0));
        assertThat(snvStaticCons.isZeroBased(), equalTo(true));

        assertThat(GenomicRegion.compare(snvStaticCons, snv), equalTo(0));
        assertThat(snvStaticCons, equalTo(snv));
    }

    @Test
    public void mnv() {
        Variant mnv = DefaultVariant.oneBased(chr1, 1, "AT", "TG");

        assertThat(mnv.startPosition(), equalTo(Position.of(1)));
        assertThat(mnv.start(), equalTo(1));
        assertThat(mnv.endPosition(), equalTo(Position.of(2)));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    public void mnvZeroBased() {
        Variant mnv = DefaultVariant.zeroBased(chr1, 0, "AT", "TG");

        assertThat(mnv.startPosition(), equalTo(Position.of(0)));
        assertThat(mnv.start(), equalTo(0));
        assertThat(mnv.endPosition(), equalTo(Position.of(2)));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    public void del() {
        Variant del = DefaultVariant.oneBased(chr1, 1, "AG", "A");

        assertThat(del.startPosition(), equalTo(Position.of(1)));
        assertThat(del.endPosition(), equalTo(Position.of(2)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
    }

    @Test
    public void delZeroBased() {
        Variant del = DefaultVariant.zeroBased(chr1, 0, "AG", "A");

        assertThat(del.startPosition(), equalTo(Position.of(0)));
        assertThat(del.endPosition(), equalTo(Position.of(2)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
    }

    @Test
    public void ins() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, "A", "AG");

        assertThat(ins.startPosition(), equalTo(Position.of(1)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    public void insZeroBased() {
        Variant ins = DefaultVariant.zeroBased(chr1, 0, "A", "AG");

        assertThat(ins.startPosition(), equalTo(Position.of(0)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    public void insWithSameStrand() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, "A", "AG");
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    public void insWithNegativeStrand() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, "A", "AG");
        Variant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.contig(), equalTo(chr1));
        assertThat(negativeIns.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.startPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.endPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.ref(), equalTo("T"));
        assertThat(negativeIns.alt(), equalTo("CT"));
        assertThat(negativeIns.variantType(), equalTo(VariantType.INS));
        assertThat(negativeIns.length(), equalTo(1));
    }

    @Test
    public void delWithNegativeStrand() {
        Contig chr5 = TestContig.of(5, 5);
        Variant del = DefaultVariant.oneBased(chr5, 1, "AG", "A");
        Variant negativeDel = del.withStrand(Strand.NEGATIVE);

        assertThat(negativeDel.contig(), equalTo(chr5));
        assertThat(negativeDel.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeDel.startPosition(), equalTo(Position.of(4)));
        assertThat(negativeDel.endPosition(), equalTo(Position.of(5)));
        assertThat(negativeDel.ref(), equalTo("CT"));
        assertThat(negativeDel.alt(), equalTo("T"));
        assertThat(negativeDel.variantType(), equalTo(VariantType.DEL));
        assertThat(negativeDel.length(), equalTo(2));
        assertThat(negativeDel.refLength(), equalTo(2));
        assertThat(negativeDel.changeLength(), equalTo(-1));
    }

    @Test
    public void delZeroBasedWithNegativeStrand() {
        Contig chr5 = TestContig.of(5, 5);
        Variant del = DefaultVariant.zeroBased(chr5, 0, "AG", "A");
        Variant negativeDel = del.withStrand(Strand.NEGATIVE);

        assertThat(negativeDel.contig(), equalTo(chr5));
        assertThat(negativeDel.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeDel.startPosition(), equalTo(Position.of(3)));
        assertThat(negativeDel.endPosition(), equalTo(Position.of(5)));
        assertThat(negativeDel.ref(), equalTo("CT"));
        assertThat(negativeDel.alt(), equalTo("T"));
        assertThat(negativeDel.variantType(), equalTo(VariantType.DEL));
        assertThat(negativeDel.length(), equalTo(2));
        assertThat(negativeDel.refLength(), equalTo(2));
        assertThat(negativeDel.changeLength(), equalTo(-1));
    }

    @Test
    public void delLenSvLen() {
        Variant del = DefaultVariant.oneBased(chr1, "rs2376870", 2827694, "CGTGGATGCGGGGAC", "C");
        //.    PASS   SVTYPE=DEL;LEN=15;HOMLEN=1;HOMSEQ=G;SVLEN=-14
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(15));
        assertThat(del.refLength(), equalTo(15));
        assertThat(del.changeLength(), equalTo(-14));
    }

    @Test
    public void snvToOppositeStrand() {
        Variant snv = DefaultVariant.oneBased(chr1, 1, "A", "T");
        assertThat(snv.strand(), equalTo(Strand.POSITIVE));
        Variant oppositeSnv = snv.toOppositeStrand();
        assertThat(oppositeSnv.contig(), equalTo(chr1));
        assertThat(oppositeSnv.strand(), equalTo(Strand.NEGATIVE));
        assertThat(oppositeSnv.startPosition(), equalTo(Position.of(1000)));
        assertThat(oppositeSnv.endPosition(), equalTo(Position.of(1000)));
        assertThat(oppositeSnv.ref(), equalTo("T"));
        assertThat(oppositeSnv.alt(), equalTo("A"));
        assertThat(oppositeSnv.variantType(), equalTo(VariantType.SNV));
        assertThat(oppositeSnv.length(), equalTo(1));
        assertThat(oppositeSnv.refLength(), equalTo(1));
        assertThat(oppositeSnv.changeLength(), equalTo(0));
    }

    @Test
    public void DefaultVariantContainsSnv() {
        Variant largeIns = DefaultVariant.oneBased(chr1, 1, 100, "T", "<INS>", 100);
        assertTrue(largeIns.contains(SequenceVariant.oneBased(chr1, 1, "A", "T")));
        assertTrue(largeIns.contains(SequenceVariant.zeroBased(chr1, 0, "A", "T")));
        assertFalse(largeIns.contains(SequenceVariant.oneBased(chr1, 200, "C", "A")));
        assertTrue(largeIns.contains(PartialBreakend.oneBased(chr1, "bnd_A", Strand.POSITIVE, Position.of(1))));
    }

    @Test
    public void DefaultVariantOverlapsOther() {
        Variant largeIns = DefaultVariant.oneBased(chr1, 1, 100, "T", "<INS>", 100);
        Variant otherIns = DefaultVariant.oneBased(chr1, 99, 299, "C", "<INS>", 200);
        assertTrue(largeIns.overlapsWith(otherIns));
        assertTrue(otherIns.overlapsWith(largeIns));
    }

    @Test
    public void symbolicThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> DefaultVariant.oneBased(chr1, 1, 1, "A", "A[1:2]", 1));
    }

    @Test
    public void throwsIllegalArgumentWithNonSymbolicAllele() {
        // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
        DefaultVariant instance = DefaultVariant.oneBased(chr1, 1, 1, "A", "T", 1);
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.startPosition(), equalTo(Position.of(1)));
        assertThat(instance.endPosition(), equalTo(Position.of(1)));
        assertThat(instance.variantType(), equalTo(VariantType.SNV));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("T"));
        assertThat(instance.refLength(), equalTo(1));
        assertThat(instance.changeLength(), equalTo(1));
    }

    @Test
    public void shouldBeSymbolic() {
        Variant instance = DefaultVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        assertThat(instance.isSymbolic(), equalTo(true));
    }

    @Test
    public void symbolicDel() {
        Variant del = DefaultVariant.oneBased(chr1, 1, 100, "A", "<DEL>", -99);

        assertThat(del.contig(), equalTo(chr1));
        assertThat(del.startPosition(), equalTo(Position.of(1)));
        assertThat(del.endPosition(), equalTo(Position.of(100)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(100));
        assertThat(del.ref(), equalTo("A"));
        assertThat(del.alt(), equalTo("<DEL>"));
        assertThat(del.refLength(), equalTo(100));
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
        Variant del = DefaultVariant.zeroBased(chr1, 0, 100, "A", "<DEL>", -99);

        assertThat(del.contig(), equalTo(chr1));
        assertThat(del.startPosition(), equalTo(Position.of(0)));
        assertThat(del.endPosition(), equalTo(Position.of(100)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(100));
        assertThat(del.ref(), equalTo("A"));
        assertThat(del.alt(), equalTo("<DEL>"));
        assertThat(del.refLength(), equalTo(100));
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
        Variant del = DefaultVariant.oneBased(chr1, 321682, 321682 + 205, "T", "<DEL>", -205);
        assertThat(del.length(), equalTo(206));
        assertThat(del.refLength(), equalTo(206));
        assertThat(del.changeLength(), equalTo(-205));
    }

    @Test
    public void symbolicIns() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);

        assertThat(ins.contig(), equalTo(chr1));
        assertThat(ins.startPosition(), equalTo(Position.of(1)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.ref(), equalTo("A"));
        assertThat(ins.alt(), equalTo("<INS>"));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(100));
    }

    @Test
    public void symbolicInsWithSameStrand() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    public void symbolicInsWithNegativeStrand() {
        Variant ins = DefaultVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        Variant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.contig(), equalTo(chr1));
        assertThat(negativeIns.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.startPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.endPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.ref(), equalTo("T"));
        assertThat(negativeIns.alt(), equalTo("<INS>"));
        assertThat(negativeIns.variantType(), equalTo(VariantType.INS));
        assertThat(negativeIns.length(), equalTo(1));
        assertThat(negativeIns.changeLength(), equalTo(100));
    }

    @Test
    public void symbolicDelWithNegativeStrand() {
        Variant instance = DefaultVariant.oneBased(chr1, 1, 100, "A", "<DEL>", -99);
        Variant negative = instance.withStrand(Strand.NEGATIVE);

        assertThat(negative.contig(), equalTo(chr1));
        assertThat(negative.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negative.startPosition(), equalTo(Position.of(901)));
        assertThat(negative.endPosition(), equalTo(Position.of(1000)));
        assertThat(negative.ref(), equalTo("T"));
        assertThat(negative.alt(), equalTo("<DEL>"));
        assertThat(negative.variantType(), equalTo(VariantType.DEL));
        assertThat(negative.length(), equalTo(100));
        assertThat(negative.changeLength(), equalTo(-99));
    }

    @Test
    public void symbolicDelZeroBasedWithNegativeStrand() {
        Variant instance = DefaultVariant.zeroBased(chr1, 0, 100, "A", "<DEL>", -99);
        Variant negative = instance.withStrand(Strand.NEGATIVE);

        assertThat(negative.contig(), equalTo(chr1));
        assertThat(negative.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negative.startPosition(), equalTo(Position.of(900)));
        assertThat(negative.endPosition(), equalTo(Position.of(1000)));
        assertThat(negative.ref(), equalTo("T"));
        assertThat(negative.alt(), equalTo("<DEL>"));
        assertThat(negative.variantType(), equalTo(VariantType.DEL));
        assertThat(negative.length(), equalTo(100));
        assertThat(negative.changeLength(), equalTo(-99));
    }

    @Test
    public void compareWithGenomicRegion() {
        GenomicRegion region = GenomicRegion.zeroBased(chr1, 0, 10);
        Variant variant = DefaultVariant.zeroBased(chr1, 1, "A", "TAA");
        assertThat(variant.isSymbolic(), is(false));
        assertThat(GenomicRegion.compare(region, variant), equalTo(-1));
        assertThat(region.contains(variant), equalTo(true));
    }

    @Test
    public void canCreateSymbolicBnd() {
        Variant instance = DefaultVariant.oneBased(chr1, 1, 1, "A", "<BND>", 0);
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.startPosition(), equalTo(Position.of(1)));
        assertThat(instance.endPosition(), equalTo(Position.of(1)));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("<BND>"));
        assertThat(instance.variantType(), equalTo(VariantType.BND));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.changeLength(), equalTo(0));
    }

    @Test
    public void isSymbolic() {
        assertThat(DefaultVariant.oneBased(chr1, 1, "A", "TAA").isSymbolic(), is(false));
        assertThat(DefaultVariant.oneBased(chr1, 1, 100, "A", "<DEL>", -99).isSymbolic(), is(true));
        assertThat(DefaultVariant.oneBased(chr1, 1, 1, "A", "<BND>", 0).isSymbolic(), is(true));
    }

}