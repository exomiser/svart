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
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.oneBased(chr1, 1, "A", "<INS>"));
    }

    @Test
    void throwsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> SequenceVariant.oneBased(chr1, 1, "A", "A[1:2]"));
    }

    @Test
    void shouldNotBeSymbolic() {
        Variant instance = SequenceVariant.oneBased(chr1, 1, "A", "T");
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    void snv() {
        Variant snv = SequenceVariant.oneBased(chr1, 1, "A", "T");

        assertThat(snv.startPosition(), equalTo(snv.endPosition()));
        assertThat(snv.variantType(), equalTo(VariantType.SNV));
        assertThat(snv.length(), equalTo(1));
    }

    @Test
    void mnv() {
        Variant mnv = SequenceVariant.oneBased(chr1, 1, "AT", "TG");

        assertThat(mnv.startPosition(), equalTo(Position.of(1)));
        assertThat(mnv.start(), equalTo(1));
        assertThat(mnv.endPosition(), equalTo(Position.of(2)));
        assertThat(mnv.variantType(), equalTo(VariantType.MNV));
        assertThat(mnv.length(), equalTo(2));
    }

    @Test
    void del() {
        Variant del = SequenceVariant.oneBased(chr1, 1, "AG", "A");

        assertThat(del.startPosition(), equalTo(Position.of(1)));
        assertThat(del.endPosition(), equalTo(Position.of(2)));
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(2));
    }

    @Test
    void ins() {
        Variant ins = SequenceVariant.oneBased(chr1, 1, "A", "AG");

        assertThat(ins.startPosition(), equalTo(Position.of(1)));
        assertThat(ins.endPosition(), equalTo(Position.of(1)));
        assertThat(ins.variantType(), equalTo(VariantType.INS));
        assertThat(ins.length(), equalTo(1));
        assertThat(ins.refLength(), equalTo(1));
        assertThat(ins.changeLength(), equalTo(1));
    }

    @Test
    void insWithSameStrand() {
        Variant ins = SequenceVariant.oneBased(chr1, 1, "A", "AG");
        assertSame(ins, ins.withStrand(Strand.POSITIVE));
    }

    @Test
    void insWithNegativeStrand() {
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
    void delLenSvLen() {
        Variant del = SequenceVariant.oneBased(chr1, "rs2376870", 2827694, "CGTGGATGCGGGGAC", "C");
       //.    PASS   SVTYPE=DEL;LEN=15;HOMLEN=1;HOMSEQ=G;SVLEN=-14
        assertThat(del.variantType(), equalTo(VariantType.DEL));
        assertThat(del.length(), equalTo(15));
        assertThat(del.refLength(), equalTo(15));
        assertThat(del.changeLength(), equalTo(-14));
    }

    @Test
    void snvToOppositeStrand() {
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
    void symbolicVariantContainsSnv() {
        Variant largeIns = SymbolicVariant.of(chr1, 1, 100, "T", "<INS>", 100);
        assertTrue(largeIns.contains(SequenceVariant.oneBased(chr1, 1, "A", "T")));
        assertFalse(largeIns.contains(SequenceVariant.oneBased(chr1, 200, "C", "A")));
        assertTrue(largeIns.contains(PartialBreakend.of(chr1, Position.of(1), Strand.POSITIVE, "T")));
    }

}