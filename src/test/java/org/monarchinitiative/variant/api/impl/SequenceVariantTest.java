package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SequenceVariantTest {

    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 1000, "", "", "chr1");

    @Test
    public void throwsIllegalArgumentWithSymbolicAllele() {
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.oneBased(chr1, 1, "A", "<INS>"));
    }

    @Test
    public void throwsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.oneBased(chr1, 1, "A", "A[1:2]"));
    }

    @Test
    public void shouldNotBeSymbolic() {
        Variant instance = SequenceVariant.oneBased(chr1, 1, "A", "T");
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    public void snvOneBased() {
        Variant snv = SequenceVariant.oneBased(chr1, 1, "A", "T");

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
        Variant snv = SequenceVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(0), "A", "T");

        assertThat(snv.startPosition(), equalTo(Position.of(0)));
        assertThat(snv.endPosition(), equalTo(Position.of(1)));
        assertThat(snv.variantType(), equalTo(VariantType.SNV));
        assertThat(snv.length(), equalTo(1));
        assertThat(snv.refLength(), equalTo(1));
        assertThat(snv.changeLength(), equalTo(0));
        assertThat(snv.isZeroBased(), equalTo(true));

        Variant snvStaticCons = SequenceVariant.zeroBased(chr1, 0, "A", "T");
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
        Variant mnv = SequenceVariant.oneBased(chr1, 1, "AT", "TG");

        assertThat(mnv.startPosition(), equalTo(Position.of(1)));
        assertThat(mnv.start(), equalTo(1));
        assertThat(mnv.endPosition(), equalTo(Position.of(2)));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    public void mnvZeroBased() {
        Variant mnv = SequenceVariant.zeroBased(chr1, 0, "AT", "TG");

        assertThat(mnv.startPosition(), equalTo(Position.of(0)));
        assertThat(mnv.start(), equalTo(0));
        assertThat(mnv.endPosition(), equalTo(Position.of(2)));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    public void del() {
        Variant del = SequenceVariant.oneBased(chr1, 1, "AG", "A");

        assertThat(del.startPosition(), equalTo(Position.of(1)));
        assertThat(del.endPosition(), equalTo(Position.of(2)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
    }

    @Test
    public void delZeroBased() {
        Variant del = SequenceVariant.zeroBased(chr1, 0, "AG", "A");

        assertThat(del.startPosition(), equalTo(Position.of(0)));
        assertThat(del.endPosition(), equalTo(Position.of(2)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
    }

    @Test
    public void ins() {
        Variant ins = SequenceVariant.oneBased(chr1, 1, "A", "AG");

        assertThat(ins.startPosition(), equalTo(Position.of(1)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    public void insZeroBased() {
        Variant ins = SequenceVariant.zeroBased(chr1, 0, "A", "AG");

        assertThat(ins.startPosition(), equalTo(Position.of(0)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    public void insWithSameStrand() {
        Variant ins = SequenceVariant.oneBased(chr1, 1, "A", "AG");
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    public void insWithNegativeStrand() {
        Variant ins = SequenceVariant.oneBased(chr1, 1, "A", "AG");
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
        Contig chr5 = Contig.of(5, "5", SequenceRole.UNKNOWN, 5, "", "", "");
        Variant del = SequenceVariant.oneBased(chr5, 1, "AG", "A");
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
        Contig chr5 = Contig.of(5, "5", SequenceRole.UNKNOWN, 5, "", "", "");
        Variant del = SequenceVariant.zeroBased(chr5, 0, "AG", "A");
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
        Variant del = SequenceVariant.oneBased(chr1, "rs2376870", 2827694, "CGTGGATGCGGGGAC", "C");
       //.    PASS   SVTYPE=DEL;LEN=15;HOMLEN=1;HOMSEQ=G;SVLEN=-14
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(15));
        assertThat(del.refLength(), equalTo(15));
        assertThat(del.changeLength(), equalTo(-14));
    }

    @Test
    public void snvToOppositeStrand() {
        Variant snv = SequenceVariant.oneBased(chr1, 1, "A", "T");
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
    public void symbolicVariantContainsSnv() {
        Variant largeIns = SymbolicVariant.oneBased(chr1, 1, 100, "T", "<INS>", 100);
        assertTrue(largeIns.contains(SequenceVariant.oneBased(chr1, 1, "A", "T")));
        assertTrue(largeIns.contains(SequenceVariant.zeroBased(chr1, 0, "A", "T")));
        assertFalse(largeIns.contains(SequenceVariant.oneBased(chr1, 200, "C", "A")));
        assertTrue(largeIns.contains(PartialBreakend.oneBased(chr1, "bnd_A", Strand.POSITIVE, Position.of(1))));
    }

}