package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SequenceVariantTest {

    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 1000, "", "", "chr1");

    @Test
    void throwsIllegalArgumentWithSymbolicAllele() {
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.of(chr1, 1, "A", "<INS>"));
    }

    @Test
    void throwsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.of(chr1, 1, "A", "A[1:2]"));
    }

    @Test
    void shouldNotBeSymbolic() {
        Variant instance = SequenceVariant.of(chr1, 1, "A", "T");
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    void snv() {
        Variant snv = SequenceVariant.of(chr1, 1, "A", "T");

        assertThat(snv.getStartPosition(), equalTo(snv.getEndPosition()));
        assertThat(snv.getType(), equalTo(VariantType.SNV));
        assertThat(snv.getLength(), equalTo(1));
    }

    @Test
    void mnv() {
        Variant mnv = SequenceVariant.of(chr1, 1, "AT", "TG");

        assertThat(mnv.getStartPosition(), equalTo(Position.of(1)));
        assertThat(mnv.getStart(), equalTo(1));
        assertThat(mnv.getEndPosition(), equalTo(Position.of(2)));
        assertThat(mnv.getType(), equalTo(VariantType.MNV));
        assertThat(mnv.getLength(), equalTo(2));
    }

    @Test
    void del() {
        Variant del = SequenceVariant.of(chr1, 1, "AG", "A");

        assertThat(del.getStartPosition(), equalTo(Position.of(1)));
        assertThat(del.getEndPosition(), equalTo(Position.of(2)));
        assertThat(del.getType(), equalTo(VariantType.DEL));
        assertThat(del.getLength(), equalTo(2));
    }

    @Test
    void ins() {
        Variant ins = SequenceVariant.of(chr1, 1, "A", "AG");

        assertThat(ins.getStartPosition(), equalTo(Position.of(1)));
        assertThat(ins.getEndPosition(), equalTo(Position.of(1)));
        assertThat(ins.getType(), equalTo(VariantType.INS));
        assertThat(ins.getLength(), equalTo(1));
    }

    @Test
    void insWithSameStrand() {
        Variant ins = SequenceVariant.of(chr1, 1, "A", "AG");
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void insWithNegativeStrand() {
        Variant ins = SequenceVariant.of(chr1, 1, "A", "AG");
        Variant negativeIns = ins.withStrand(Strand.NEGATIVE);

        assertThat(negativeIns.getContig(), equalTo(chr1));
        assertThat(negativeIns.getStrand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeIns.getStartPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.getEndPosition(), equalTo(Position.of(1000)));
        assertThat(negativeIns.getRef(), equalTo("T"));
        assertThat(negativeIns.getAlt(), equalTo("CT"));
        assertThat(negativeIns.getType(), equalTo(VariantType.INS));
        assertThat(negativeIns.getLength(), equalTo(1));
    }

    @Test
    public void delWithNegativeStrand() {
        Variant del = SequenceVariant.of(chr1, 1, "AG", "A");
        Variant negativeDel = del.withStrand(Strand.NEGATIVE);

        assertThat(negativeDel.getContig(), equalTo(chr1));
        assertThat(negativeDel.getStrand(), equalTo(Strand.NEGATIVE));
        assertThat(negativeDel.getStartPosition(), equalTo(Position.of(999)));
        assertThat(negativeDel.getEndPosition(), equalTo(Position.of(1000)));
        // TODO: this is unexpected behavior
        assertThat(negativeDel.getRef(), equalTo("CT"));
        assertThat(negativeDel.getAlt(), equalTo("T"));
        assertThat(negativeDel.getType(), equalTo(VariantType.DEL));
        assertThat(negativeDel.getLength(), equalTo(2));
    }

    @Test
    void snvToOppositeStrand() {
        Variant snv = SequenceVariant.of(chr1, 1, "A", "T");
        assertThat(snv.getStrand(), equalTo(Strand.POSITIVE));
        Variant oppositeSnv = snv.toOppositeStrand();
        assertThat(oppositeSnv.getContig(), equalTo(chr1));
        assertThat(oppositeSnv.getStrand(), equalTo(Strand.NEGATIVE));
        assertThat(oppositeSnv.getStartPosition(), equalTo(Position.of(1000)));
        assertThat(oppositeSnv.getEndPosition(), equalTo(Position.of(1000)));
        assertThat(oppositeSnv.getRef(), equalTo("T"));
        assertThat(oppositeSnv.getAlt(), equalTo("A"));
        assertThat(oppositeSnv.getType(), equalTo(VariantType.SNV));
        assertThat(oppositeSnv.getLength(), equalTo(1));}

    @Test
    void symbolicVariantContainsSnv() {
        Variant largeIns = SymbolicVariant.of(chr1, 1, 100, "T", "<INS>", 100, VariantType.INS);
        assertTrue(largeIns.contains(SequenceVariant.of(chr1, 1, "A", "T")));
        assertFalse(largeIns.contains(SequenceVariant.of(chr1, 200, "C", "A")));
        assertTrue(largeIns.contains(PartialBreakend.of(chr1, Position.of(1), Strand.POSITIVE, "T")));
    }

}