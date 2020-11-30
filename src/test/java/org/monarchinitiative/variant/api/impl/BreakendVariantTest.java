package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.monarchinitiative.variant.api.impl.TestVariants.*;

/**
 * The tests use examples listed in section 5.4. of
 * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF 4.2</a> specs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class BreakendVariantTest {


    @Test
    public void properties() {
        BreakendVariant bnd_U = bnd_U();
        assertThat(bnd_U.isSymbolic(), equalTo(true));
        assertThat(bnd_U.variantType(), equalTo(VariantType.BND));
        assertThat(bnd_U.id(), equalTo("bnd_U"));
        assertThat(bnd_U.mateId(), equalTo("bnd_V"));
        assertThat(bnd_U.eventId(), equalTo("tra2"));
    }

    @Test
    public void tra2_bnd_U() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant bnd_U = bnd_U();
        assertThat(bnd_U.eventId(), equalTo("tra2"));
        assertThat(bnd_U.ref(), equalTo("C"));
        assertThat(bnd_U.alt(), equalTo(""));
        assertThat(bnd_U.refLength(), equalTo(1));
        assertThat(bnd_U.length(), equalTo(0));
        assertThat(bnd_U.changeLength(), equalTo(0));

        Breakend left = bnd_U.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.position(), equalTo(Position.of(123_456)));
        assertThat(left.id(), equalTo("bnd_U"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = bnd_U.right();
        assertThat(right.contig(), equalTo(chr2));
        assertThat(right.position(), equalTo(Position.of(321_682)));
        assertThat(right.id(), equalTo("bnd_V"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra1_bnd_W() {
        // 2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1
        BreakendVariant bnd_W = bnd_W();
        assertThat(bnd_W.eventId(), equalTo("tra1"));
        assertThat(bnd_W.ref(), equalTo("G"));
        assertThat(bnd_W.alt(), equalTo(""));
        assertThat(bnd_W.refLength(), equalTo(1));
        assertThat(bnd_W.length(), equalTo(0));
        assertThat(bnd_W.changeLength(), equalTo(0));

        Breakend left = bnd_W.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.position(), equalTo(Position.of(321_681)));
        assertThat(left.id(), equalTo("bnd_W"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = bnd_W.right();
        assertThat(right.contig(), equalTo(chr17));
        assertThat(right.position(), equalTo(Position.of(chr17.length() - 198_982 + 1)));
        assertThat(right.id(), equalTo("bnd_Y"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra2_bnd_V() {
        // 2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U;EVENT=tra2
        BreakendVariant bnd_V = bnd_V();
        assertThat(bnd_V.eventId(), equalTo("tra2"));
        assertThat(bnd_V.ref(), equalTo("T"));
        assertThat(bnd_V.alt(), equalTo(""));
        assertThat(bnd_V.refLength(), equalTo(1));
        assertThat(bnd_V.length(), equalTo(0));
        assertThat(bnd_V.changeLength(), equalTo(0));

        Breakend left = bnd_V.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.position(), equalTo(Position.of(chr2.length() - 321_682 + 1)));
        assertThat(left.id(), equalTo("bnd_V"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = bnd_V.right();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.position(), equalTo(Position.of(chr13.length() - 123_456 + 1)));
        assertThat(right.id(), equalTo("bnd_U"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra3_bnd_X() {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant bnd_X = bnd_X();
        assertThat(bnd_X.eventId(), equalTo("tra3"));
        assertThat(bnd_X.ref(), equalTo("A"));
        assertThat(bnd_X.alt(), equalTo(""));
        assertThat(bnd_X.refLength(), equalTo(1));
        assertThat(bnd_X.length(), equalTo(0));
        assertThat(bnd_X.changeLength(), equalTo(0));

        Breakend left = bnd_X.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.position(), equalTo(Position.of(chr13.length() - 123_457 + 1)));
        assertThat(left.id(), equalTo("bnd_X"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = bnd_X.right();
        assertThat(right.contig(), equalTo(chr17));
        assertThat(right.position(), equalTo(Position.of(198_983)));
        assertThat(right.id(), equalTo("bnd_Z"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void withStrand() {
        // test conversion of the NEGATIVE->POSITIVE case as this is the most complicated case
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant bnd_X = bnd_X();
        assertThat(bnd_X.withStrand(Strand.NEGATIVE), sameInstance(bnd_X));

        BreakendVariant breakendVariant = bnd_X.withStrand(Strand.POSITIVE);
        assertThat(breakendVariant.eventId(), equalTo("tra3"));
        assertThat(breakendVariant.ref(), equalTo(""));
        assertThat(breakendVariant.alt(), equalTo(""));
        assertThat(breakendVariant.refLength(), equalTo(0));
        assertThat(breakendVariant.length(), equalTo(0));
        assertThat(breakendVariant.changeLength(), equalTo(0));

        Breakend left = breakendVariant.left();
        assertThat(left.contig(), equalTo(chr17));
        assertThat(left.position(), equalTo(Position.of(chr17.length() - 198_983 + 1)));
        assertThat(left.id(), equalTo("bnd_Z"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = breakendVariant.right();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.position(), equalTo(Position.of(123_457)));
        assertThat(right.id(), equalTo("bnd_X"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @ParameterizedTest
    @CsvSource({
            "NEGATIVE,   NEGATIVE,   POSITIVE,   115046422, 198983", // no-op
            "POSITIVE,   NEGATIVE,   POSITIVE,    83058459, 123457",
            "UNSTRANDED, UNSTRANDED, UNSTRANDED, 115046422, 198983",
            "UNKNOWN,    UNKNOWN,    UNKNOWN,    115046422, 198983"})
    public void withStrand_allStrands(Strand target,
                                      Strand expectedLeftStrand, Strand expectedRightStrand,
                                      int expectedLeftPos, int expectedRightPos) {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant variant = bnd_X();

        BreakendVariant stranded = variant.withStrand(target);
        assertThat(stranded.left().position(), equalTo(Position.of(expectedLeftPos)));
        assertThat(stranded.left().strand(), equalTo(expectedLeftStrand));
        assertThat(stranded.right().position(), equalTo(Position.of(expectedRightPos)));
        assertThat(stranded.right().strand(), equalTo(expectedRightStrand));
    }

    @Test
    public void withStrand_checkRefAndAlt() {
        // 13  123456  bnd_U   C   CAGTNNNNNCA[2:321682[    6   PASS    SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant variant = bnd_U_withInserted();

        BreakendVariant positive = variant.withStrand(Strand.POSITIVE);
        assertThat(positive, sameInstance(variant));
        assertThat(positive.ref(), equalTo("C"));
        assertThat(positive.alt(), equalTo("AGTNNNNNCA"));

        BreakendVariant negative = positive.withStrand(Strand.NEGATIVE);
        assertThat(negative.ref(), equalTo(""));
        assertThat(negative.alt(), equalTo("TGNNNNNACT"));

        BreakendVariant positiveAgain = negative.withStrand(Strand.POSITIVE);
        assertThat(positiveAgain, not(sameInstance(positive)));
        assertThat(positiveAgain.ref(), equalTo("C"));
        assertThat(positiveAgain.alt(), equalTo("AGTNNNNNCA"));
    }

    @Test
    public void withCoordinateSystem() {
        BreakendVariant bnd_X = bnd_X();
        assertThat(bnd_X.withCoordinateSystem(CoordinateSystem.ONE_BASED), sameInstance(bnd_X));

        BreakendVariant breakendVariant = bnd_X.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(breakendVariant.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        Breakend left = breakendVariant.left();
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        Breakend right = breakendVariant.right();
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @Test
    public void singleBreakend() {
        // Single breakends are breakends that are not part of a novel adjacency.
        // See section 5.4.9 of VCF v4.2 specs for more info.

        // 2  321681  bnd_W   G   G.    6   PASS    SVTYPE=BND
        BreakendVariant instance = bnd_W_rightUnresolved();

        Breakend l = instance.left();
        assertThat(l.contig(), equalTo(chr2));
        assertThat(l.position(), equalTo(Position.of(321_681)));
        assertThat(l.id(), equalTo("bnd_W"));
        assertThat(l.strand(), equalTo(Strand.POSITIVE));
        assertThat(l.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend r = instance.right();
        assertThat(r.isUnresolved(), equalTo(true));

        // 13  123457  bnd_X   A   .A    6   PASS    SVTYPE=BND
        instance = TestVariants.bnd_X_leftUnresolved();
        assertThat(instance.eventId(), equalTo(""));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo(""));

        l = instance.left();
        assertThat(l.isUnresolved(), equalTo(true));

        r = instance.right();
        assertThat(r.contig(), equalTo(chr13));
        assertThat(r.position(), equalTo(Position.of(123_457)));
        assertThat(r.id(), equalTo("bnd_X"));
        assertThat(r.strand(), equalTo(Strand.POSITIVE));
        assertThat(r.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void insertedSequence_PosPos() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 13  123456  bndU  C  CAGTNNNNNCA[2:321682[  6  PASS  SVTYPE=BND;MATEID=bndV
        Breakend left = PartialBreakend.oneBased("bndU", chr13, Strand.POSITIVE, Position.of(123_456));
        Breakend right = PartialBreakend.oneBased("bndV", chr2, Strand.POSITIVE, Position.of(321_682));

        // ref stays the same, while alt is stripped of bases shared with ref
        BreakendVariant variant = new BreakendVariant("", left, right, "C", "AGTNNNNNCA");

        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("C"));
        assertThat(variant.alt(), equalTo("AGTNNNNNCA"));

        assertThat(variant.withStrand(Strand.POSITIVE), sameInstance(variant));

        variant = variant.withStrand(Strand.NEGATIVE);
        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo(""));
        assertThat(variant.alt(), equalTo("TGNNNNNACT")); // reverse complement

        Breakend l = variant.left();
        assertThat(l.contig(), equalTo(chr2));
        assertThat(l.position(), equalTo(Position.of(chr2.length() - 321_682 + 1)));
        assertThat(l.id(), equalTo("bndV"));
        assertThat(l.strand(), equalTo(Strand.NEGATIVE));
        assertThat(l.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend r = variant.right();
        assertThat(r.contig(), equalTo(chr13));
        assertThat(r.position(), equalTo(Position.of(chr13.length() - 123_456 + 1)));
        assertThat(r.id(), equalTo("bndU"));
        assertThat(r.strand(), equalTo(Strand.NEGATIVE));
        assertThat(r.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void insertedSequence_NegNeg() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 2  321682  bndV  T  ]13:123456]AGTNNNNNCAT  6  PASS  SVTYPE=BND;MATEID=bndU
        Breakend left = PartialBreakend.oneBased("bndV", chr2, Strand.POSITIVE, Position.of(321_682)).withStrand(Strand.NEGATIVE);
        Breakend right = PartialBreakend.oneBased("bndU", chr13, Strand.POSITIVE, Position.of(123_456)).withStrand(Strand.NEGATIVE);

        // ref stays the same, while alt is reverse-complemented and stripped of bases shared with ref
        BreakendVariant variant = new BreakendVariant("", left, right, "T", "TGNNNNNACT");

        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("T"));
        assertThat(variant.alt(), equalTo("TGNNNNNACT"));

        assertThat(variant.withStrand(Strand.NEGATIVE), sameInstance(variant));

        variant = variant.withStrand(Strand.POSITIVE);
        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo(""));
        assertThat(variant.alt(), equalTo("AGTNNNNNCA")); // reverse complement

        Breakend l = variant.left();
        assertThat(l.contig(), equalTo(chr13));
        assertThat(l.position(), equalTo(Position.of(123_456)));
        assertThat(l.id(), equalTo("bndU"));
        assertThat(l.strand(), equalTo(Strand.POSITIVE));
        assertThat(l.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend r = variant.right();
        assertThat(r.contig(), equalTo(chr2));
        assertThat(r.position(), equalTo(Position.of(321_682)));
        assertThat(r.id(), equalTo("bndV"));
        assertThat(r.strand(), equalTo(Strand.POSITIVE));
        assertThat(r.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }
}