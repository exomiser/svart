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
        Variant largeIns = SymbolicVariant.of(chr1, 1, 100, "T", "<INS>", 100, VariantType.INS);
        Variant otherIns = SymbolicVariant.of(chr1, 99, 299, "C", "<INS>", 200, VariantType.INS);
        assertTrue(largeIns.overlapsWith(otherIns));
        assertTrue(otherIns.overlapsWith(largeIns));
    }


    @Test
    void throwsIllegalArgumentWithNonSymbolicAllele() {
        // this ought to be legal, but maybe only when called on the interface using Variant.of(...) which defers to the correct implementation
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.of(chr1, 1, 1, "A", "T", 1, VariantType.SNV));
    }

    @Test
    void symbolicThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SymbolicVariant.of(chr1, 1, 1, "A", "A[1:2]", 1, VariantType.BND));
    }

    @Test
    void shouldBeSymbolic() {
        Variant instance = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100, VariantType.INS);
        assertThat(instance.isSymbolic(), equalTo(true));
    }

    @Test
    void symbolicDel() {
        Variant del = SymbolicVariant.of(chr1, 1, 100, "A", "<DEL>", 100, VariantType.DEL);

        assertThat(del.getContig(), equalTo(chr1));
        assertThat(del.getStartPosition(), equalTo(Position.of(1)));
        assertThat(del.getEndPosition(), equalTo(Position.of(100)));
        assertThat(del.getType(), equalTo(VariantType.DEL));
        assertThat(del.getLength(), equalTo(100));
        assertThat(del.getRef(), equalTo("A"));
        assertThat(del.getAlt(), equalTo("<DEL>"));
    }

    @Test
    void symbolicIns() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100, VariantType.INS);

        assertThat(ins.getContig(), equalTo(chr1));
        assertThat(ins.getStartPosition(), equalTo(Position.of(1)));
        assertThat(ins.getEndPosition(), equalTo(Position.of(1)));
        assertThat(ins.getType(), equalTo(VariantType.INS));
        assertThat(ins.getLength(), equalTo(100));
        assertThat(ins.getRef(), equalTo("A"));
        assertThat(ins.getAlt(), equalTo("<INS>"));
    }

    @Test
    void symbolicInsWithSameStrand() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100, VariantType.INS);
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void symbolicInsWithNegativeStrand() {
        Variant ins = SymbolicVariant.of(chr1, 1, 1, "A", "<INS>", 100, VariantType.INS);
        Variant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.getContig(), equalTo(chr1));
        assertThat(negativeIns.getStrand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.getStartPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.getEndPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.getRef(), equalTo("T"));
        assertThat(negativeIns.getAlt(), equalTo("<INS>"));
        assertThat(negativeIns.getType(), equalTo(VariantType.INS));
        assertThat(negativeIns.getLength(), equalTo(100));
    }

    @Test
    void symbolicDelWithNegativeStrand() {
        Variant del = SequenceVariant.of(chr1, 1, "ATCATTTACC", "A");
        Variant instance = SymbolicVariant.of(chr1, 1, 100, "A", "<DEL>", 100, VariantType.DEL);
        Variant negative = instance.withStrand(Strand.NEGATIVE);
        System.out.println(del);
        System.out.println(instance);
        System.out.println(negative);
        assertThat(negative.getContig(), equalTo(chr1));
        assertThat(negative.getStrand(), equalTo(Strand.NEGATIVE));
        // TODO - resolve
//        assertThat(negative.getStartPosition(), equalTo(Position.of(1000)));
        assertThat(negative.getStartPosition(), equalTo(Position.of(901)));
//        assertThat(negative.getEndPosition(), equalTo(Position.of(901)));
        assertThat(negative.getEndPosition(), equalTo(Position.of(1000)));
        assertThat(negative.getRef(), equalTo("T"));
        assertThat(negative.getAlt(), equalTo("<DEL>"));
        assertThat(negative.getType(), equalTo(VariantType.DEL));
        assertThat(negative.getLength(), equalTo(100));
    }
}