package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SymbolicVariantTest {

    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 1000, "", "", "chr1");

    @Test
    public void symbolicVariantOverlapsOther() {
        Variant largeIns = SymbolicVariant.oneBased(chr1, 1, 100, "T", "<INS>", 100);
        Variant otherIns = SymbolicVariant.oneBased(chr1, 99, 299, "C", "<INS>", 200);
        assertTrue(largeIns.overlapsWith(otherIns));
        assertTrue(otherIns.overlapsWith(largeIns));
    }

    @Test
    public void throwsIllegalArgumentWithNonSymbolicAllele() {
        // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.oneBased(chr1, 1, 1, "A", "T", 1));
    }

    @Test
    public void symbolicThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.oneBased(chr1, 1, 1, "A", "A[1:2]", 1));
    }

    @Test
    public void shouldBeSymbolic() {
        Variant instance = SymbolicVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        assertThat(instance.isSymbolic(), equalTo(true));
    }

    @Test
    public void symbolicDel() {
        Variant del = SymbolicVariant.oneBased(chr1, 1, 100, "A", "<DEL>", -99);

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
        Variant del = SymbolicVariant.zeroBased(chr1, 0, 100, "A", "<DEL>", -99);

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
        Variant del = SymbolicVariant.oneBased(chr1, 321682, 321682 + 205, "T", "<DEL>", -205);
        assertThat(del.length(), equalTo(206));
        assertThat(del.refLength(), equalTo(206));
        assertThat(del.changeLength(), equalTo(-205));
    }

    @Test
    public void symbolicIns() {
        Variant ins = SymbolicVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);

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
        Variant ins = SymbolicVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    public void symbolicInsWithUnknownStrand() {
        Variant ins = SymbolicVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
        Variant expected = SymbolicVariant.of(chr1, "", Strand.UNKNOWN, CoordinateSystem.ONE_BASED, Position.of(1), Position.of(1), "A", "<INS>", 100);
        assertThat(ins.withStrand(Strand.UNKNOWN), equalTo(expected));
        assertSame(expected, expected.withStrand(Strand.POSITIVE));
    }

    @Test
    public void symbolicInsWithNegativeStrand() {
        Variant ins = SymbolicVariant.oneBased(chr1, 1, 1, "A", "<INS>", 100);
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
        Variant instance = SymbolicVariant.oneBased(chr1, 1, 100, "A", "<DEL>", -99);
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
        Variant instance = SymbolicVariant.zeroBased(chr1, 0, 100, "A", "<DEL>", -99);
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
}