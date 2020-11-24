package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SymbolicVariantTest {

    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 1000, "", "", "chr1");

    @Test
    void symbolicVariantOverlapsOther() {
        Variant largeIns = SymbolicVariant.of(chr1, 1, 100, "T", "<INS>", 100);
        Variant otherIns = SymbolicVariant.of(chr1, 99, 299, "C", "<INS>", 200);
        assertTrue(largeIns.overlapsWith(otherIns));
        assertTrue(otherIns.overlapsWith(largeIns));
    }

    @Test
    void throwsIllegalArgumentWithNonSymbolicAllele() {
        // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.of(chr1, 1, 1, "A", "T", 1));
    }

    @Test
    void symbolicThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.of(chr1, 1, 1, "A", "A[1:2]", 1));
    }

    @Test
    void shouldBeSymbolic() {
        Variant instance = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100);
        assertThat(instance.isSymbolic(), equalTo(true));
    }

    @Test
    void symbolicDel() {
        Variant del = SymbolicVariant.of(chr1, 1, 100, "A", "<DEL>", -99);

        assertThat(del.contig(), equalTo(chr1));
        assertThat(del.startPosition(), equalTo(Position.oneBased(1)));
        assertThat(del.endPosition(), equalTo(Position.oneBased(100)));
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
    void symbolicDelLenSvLen() {
        //1       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
        Variant del = SymbolicVariant.of(chr1, 321682, 321682 + 205, "T", "<DEL>", -205);
        assertThat(del.length(), equalTo(206));
        assertThat(del.refLength(), equalTo(206));
        assertThat(del.changeLength(), equalTo(-205));
    }

    @Test
    void symbolicIns() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100);

        assertThat(ins.contig(), equalTo(chr1));
        assertThat(ins.startPosition(), equalTo(Position.oneBased(1)));
        assertThat(ins.endPosition(), equalTo(Position.oneBased(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.ref(), equalTo("A"));
        assertThat(ins.alt(), equalTo("<INS>"));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(100));
    }

    @Test
    void symbolicInsWithSameStrand() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100);
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void symbolicInsWithNegativeStrand() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100);
        Variant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.contig(), equalTo(chr1));
        assertThat(negativeIns.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.startPosition(), equalTo(Position.oneBased(1000)));
        assertThat(negativeIns.endPosition(), equalTo(Position.oneBased(1000)));
        assertThat(negativeIns.ref(), equalTo("T"));
        assertThat(negativeIns.alt(), equalTo("<INS>"));
        assertThat(negativeIns.variantType(), equalTo(VariantType.INS));
        assertThat(negativeIns.length(), equalTo(1));
        assertThat(negativeIns.changeLength(), equalTo(100));
    }

    @Test
    void symbolicDelWithNegativeStrand() {
        Variant del = SequenceVariant.oneBased(chr1, 1, "ATCATTTACC", "A");
        Variant instance = SymbolicVariant.of(chr1, 1, 100, "A", "<DEL>", -99);
        Variant negative = instance.withStrand(Strand.NEGATIVE);
        System.out.println(del);
        System.out.println(instance);
        System.out.println(negative);
        assertThat(negative.contig(), equalTo(chr1));
        assertThat(negative.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negative.startPosition(), equalTo(Position.oneBased(901)));
        assertThat(negative.endPosition(), equalTo(Position.oneBased(1000)));
        assertThat(negative.ref(), equalTo("T"));
        assertThat(negative.alt(), equalTo("<DEL>"));
        assertThat(negative.variantType(), equalTo(VariantType.DEL));
        assertThat(negative.length(), equalTo(100));
        assertThat(negative.changeLength(), equalTo(-99));
    }
}