package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.monarchinitiative.svart.impl.TestVariants.*;
import static org.monarchinitiative.svart.TestContigs.*;

/**
 * The tests use examples listed in section 5.4. of
 * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF 4.2</a> specs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class DefaultBreakendVariantTest {

    @Test
    public void variantProperties() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        Variant variant = breakendVariant_UV();
        assertThat(variant.contigId(), equalTo(13));
        assertThat(variant.start(), equalTo(123_456));
        assertThat(variant.end(), equalTo(123_456));
        assertThat(variant.strand(), equalTo(Strand.POSITIVE));
        assertThat(variant.variantType(), equalTo(VariantType.BND));
        assertThat(variant.coordinateSystem(), equalTo(CoordinateSystem.oneBased()));
        assertThat(variant.isBreakend(), equalTo(true));

        assertThat(variant.refLength(), equalTo(1));
        assertThat(variant.changeLength(), equalTo(0));
        assertThat(variant.length(), equalTo(0));
    }

    @Test
    public void breakendVariantProperties() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant bnd_U = breakendVariant_UV();
        assertThat(bnd_U.isSymbolic(), equalTo(true));
        assertThat(bnd_U.variantType(), equalTo(VariantType.BND));
        assertThat(bnd_U.id(), equalTo("bnd_U"));
        assertThat(bnd_U.right().mateId(), equalTo("bnd_V"));
        assertThat(bnd_U.eventId(), equalTo("tra2"));
    }

    @Test
    public void tra2_bnd_U() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant bnd_U = breakendVariant_UV();
        assertThat(bnd_U.eventId(), equalTo("tra2"));
        assertThat(bnd_U.ref(), equalTo("C"));
        assertThat(bnd_U.alt(), equalTo(""));
        assertThat(bnd_U.refLength(), equalTo(1));
        assertThat(bnd_U.length(), equalTo(0));
        assertThat(bnd_U.changeLength(), equalTo(0));

        Breakend left = bnd_U.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.start(), equalTo(123_456));
        assertThat(left.id(), equalTo("bnd_U"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));

        Breakend right = bnd_U.right();
        assertThat(right.contig(), equalTo(chr2));
        assertThat(right.start(), equalTo(321_682));
        assertThat(right.id(), equalTo("bnd_V"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
    }

    @Test
    public void tra1_bnd_W() {
        // 2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1
        BreakendVariant bnd_W = breakendVariant_WY();
        assertThat(bnd_W.eventId(), equalTo("tra1"));
        assertThat(bnd_W.ref(), equalTo("G"));
        assertThat(bnd_W.alt(), equalTo(""));
        assertThat(bnd_W.refLength(), equalTo(1));
        assertThat(bnd_W.length(), equalTo(0));
        assertThat(bnd_W.changeLength(), equalTo(0));

        Breakend left = bnd_W.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.start(), equalTo(321_681));
        assertThat(left.id(), equalTo("bnd_W"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));

        Breakend right = bnd_W.right();
        assertThat(right.contig(), equalTo(chr17));
        assertThat(right.start(), equalTo(chr17.length() - 198_981));
        assertThat(right.id(), equalTo("bnd_Y"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
    }

    @Test
    public void tra2_bnd_V() {
        // 2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U;EVENT=tra2
        BreakendVariant bnd_V = breakendVariant_VU();
        assertThat(bnd_V.eventId(), equalTo("tra2"));
        assertThat(bnd_V.ref(), equalTo("T"));
        assertThat(bnd_V.alt(), equalTo(""));
        assertThat(bnd_V.refLength(), equalTo(1));
        assertThat(bnd_V.length(), equalTo(0));
        assertThat(bnd_V.changeLength(), equalTo(0));

        Breakend left = bnd_V.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.start(), equalTo(chr2.length() - 321_681));
        assertThat(left.id(), equalTo("bnd_V"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));

        Breakend right = bnd_V.right();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.start(), equalTo(chr13.length() - 123_456 + 1));
        assertThat(right.id(), equalTo("bnd_U"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
    }

    @Test
    public void tra3_bnd_X() {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant bnd_X = breakendVariant_XZ();
        assertThat(bnd_X.eventId(), equalTo("tra3"));
        assertThat(bnd_X.ref(), equalTo("A"));
        assertThat(bnd_X.alt(), equalTo(""));
        assertThat(bnd_X.refLength(), equalTo(1));
        assertThat(bnd_X.length(), equalTo(0));
        assertThat(bnd_X.changeLength(), equalTo(0));

        Breakend left = bnd_X.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.start(), equalTo(chr13.length() - 123_456));
        assertThat(left.id(), equalTo("bnd_X"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));

        Breakend right = bnd_X.right();
        assertThat(right.contig(), equalTo(chr17));
        assertThat(right.start(), equalTo(198_983));
        assertThat(right.id(), equalTo("bnd_Z"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
    }

    @ParameterizedTest
    @CsvSource({
            "NEGATIVE",
            "POSITIVE"})
    public void withStrand_allStrands(Strand target) {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant variant = breakendVariant_XZ();

        // withStrand is not applicable for BreakendVariant
        BreakendVariant stranded = variant.withStrand(target);
        assertThat(stranded, sameInstance(variant));
    }

    @Test
    public void toOppositeStrand() {
        // test conversion of the NEGATIVE->POSITIVE case as this is the most complicated case
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant bnd_X = breakendVariant_XZ();
        assertThat(bnd_X.left().strand(), equalTo(Strand.NEGATIVE));
        assertThat(bnd_X.right().strand(), equalTo(Strand.POSITIVE));

        BreakendVariant opposite = bnd_X.toOppositeStrand();
        assertThat(bnd_X.toOppositeStrand(), equalTo(bnd_X));

        Breakend left = bnd_X.right().toOppositeStrand();
        assertThat(left.contig(), equalTo(chr17));
        assertThat(left.start(), equalTo(chr17.length() - 198_982));
        assertThat(left.id(), equalTo("bnd_Z"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));

        Breakend right = bnd_X.left().toOppositeStrand();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.start(), equalTo(123_457));
        assertThat(right.id(), equalTo("bnd_X"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));

        BreakendVariant original = opposite.toOppositeStrand();
        assertThat(original, equalTo(bnd_X));
        assertThat(original, sameInstance(bnd_X));
    }

    @Test
    public void withStrand_checkRefAndAlt() {
        // 13  123456  bnd_U   C   CAGTNNNNNCA[2:321682[    6   PASS    SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant variant = breakendVariant_UV_withInsertion();

        BreakendVariant positive = variant.withStrand(Strand.POSITIVE);
        assertThat(positive, sameInstance(variant));
        assertThat(positive.ref(), equalTo("C"));
        assertThat(positive.alt(), equalTo("AGTNNNNNCA"));

        BreakendVariant negative = positive.toOppositeStrand();
        assertThat(negative, sameInstance(variant));

        BreakendVariant positiveAgain = negative.toOppositeStrand();
        assertThat(positiveAgain, sameInstance(positive));
    }

    @Test
    public void singleBreakend_leftUnresolved() {
        // Single breakends are breakends that are not part of a novel adjacency.
        // See section 5.4.9 of VCF v4.2 specs for more info.

        // 13  123457  bnd_X   A   .A    6   PASS    SVTYPE=BND
        BreakendVariant instance = TestVariants.bnd_X_leftUnresolved();
        assertThat(instance.changeLength(), equalTo(0));
        assertThat(instance.eventId(), equalTo(""));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo(""));

        Breakend left = instance.left();
        assertThat(left.isUnresolved(), equalTo(true));

        Breakend right = instance.right();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.start(), equalTo(123_457));
        assertThat(right.id(), equalTo("bnd_X"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));


        BreakendVariant variant = instance.toOppositeStrand();
        assertThat(variant.changeLength(), equalTo(0));

        Breakend rightOpposite = instance.right().toOppositeStrand();
        assertThat(rightOpposite.contig(), equalTo(chr13));
        assertThat(rightOpposite.start(), equalTo(chr13.length() - 123_456));
        assertThat(rightOpposite.id(), equalTo("bnd_X"));
        assertThat(rightOpposite.strand(), equalTo(Strand.NEGATIVE));

        Breakend leftOpposite = instance.left().toOppositeStrand();
        assertThat(leftOpposite.isUnresolved(), equalTo(true));
    }

    @Test
    public void singleBreakend_rightUnresolved() {
        // Single breakends are breakends that are not part of a novel adjacency.
        // See section 5.4.9 of VCF v4.2 specs for more info.

        // 2  321681  bnd_W   G   G.    6   PASS    SVTYPE=BND
        BreakendVariant instance = bnd_W_rightUnresolved();
        assertThat(instance.changeLength(), equalTo(0));

        Breakend left = instance.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.start(), equalTo(321_681));
        assertThat(left.id(), equalTo("bnd_W"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));

        Breakend leftOpposite = left.toOppositeStrand();
        assertThat(leftOpposite.contig(), equalTo(chr2));
        assertThat(leftOpposite.start(), equalTo(chr2.length() - 321_681 + 1));
        assertThat(leftOpposite.id(), equalTo("bnd_W"));
        assertThat(leftOpposite.strand(), equalTo(Strand.NEGATIVE));

        Breakend right = instance.right();
        assertThat(right.isUnresolved(), equalTo(true));
        assertThat(right.toOppositeStrand().isUnresolved(), equalTo(true));
    }

    @Test
    public void insertedSequence_PosStart() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 13  123456  bndU  C  CAGTNNNNNCA[2:321682[  6  PASS  SVTYPE=BND;MATEID=bndV
        Breakend left = DefaultBreakend.zeroBased(chr13, "bndU", Strand.POSITIVE, Position.of(123_456));
        Breakend right = DefaultBreakend.zeroBased(chr2, "bndV", Strand.POSITIVE, Position.of(321_681));

        // ref stays the same, while alt is stripped of bases shared with ref
        BreakendVariant variant = DefaultBreakendVariant.of("", left, right, "C", "AGTNNNNNCA");

        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("C"));
        assertThat(variant.alt(), equalTo("AGTNNNNNCA"));
        assertThat(variant.changeLength(), equalTo(10));
        assertThat(variant.isBreakend(), equalTo(true));


        assertThat(variant.withStrand(Strand.POSITIVE), sameInstance(variant));
        assertThat(variant.toOppositeStrand(), sameInstance(variant));

        Breakend rightOpposite = right.toOppositeStrand();
        assertThat(rightOpposite.contig(), equalTo(chr2));
        assertThat(rightOpposite.start(), equalTo(chr2.length() - 321_681));
        assertThat(rightOpposite.id(), equalTo("bndV"));
        assertThat(rightOpposite.strand(), equalTo(Strand.NEGATIVE));
        assertThat(rightOpposite.toOppositeStrand(), equalTo(variant.right()));

        Breakend leftOpposite = left.toOppositeStrand();
        assertThat(leftOpposite.contig(), equalTo(chr13));
        assertThat(leftOpposite.start(), equalTo(chr13.length() - 123_456));
        assertThat(leftOpposite.id(), equalTo("bndU"));
        assertThat(leftOpposite.strand(), equalTo(Strand.NEGATIVE));
        assertThat(leftOpposite.toOppositeStrand(), equalTo(variant.left()));
    }

    @Test
    public void insertedSequence_NegNeg() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 2  321682  bndV  T  ]13:123456]AGTNNNNNCAT  6  PASS  SVTYPE=BND;MATEID=bndU
        Breakend left = DefaultBreakend.oneBased(chr2, "bndV",Strand.POSITIVE, Position.of(321_682))
                .withStrand(Strand.NEGATIVE);
        Breakend right = DefaultBreakend.oneBased(chr13, "bndU", Strand.POSITIVE, Position.of(123_456))
                .withStrand(Strand.NEGATIVE);

        // ref stays the same, while alt is reverse-complemented and stripped of bases shared with ref
        BreakendVariant variant = BreakendVariant.of("", left, right, "T", "TGNNNNNACT");

        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("T"));
        assertThat(variant.alt(), equalTo("TGNNNNNACT"));
        assertThat(variant.changeLength(), equalTo(10));

        assertThat(variant.withStrand(Strand.NEGATIVE), sameInstance(variant));
        assertThat(variant.toOppositeStrand(), sameInstance(variant));

        Breakend leftOpposite = left.toOppositeStrand();
        assertThat(leftOpposite.contig(), equalTo(chr2));
        assertThat(leftOpposite.start(), equalTo(321_682));
        assertThat(leftOpposite.id(), equalTo("bndV"));
        assertThat(leftOpposite.strand(), equalTo(Strand.POSITIVE));
        assertThat(leftOpposite.toOppositeStrand(), equalTo(variant.left()));

        Breakend rightOpposite = right.toOppositeStrand();
        assertThat(rightOpposite.contig(), equalTo(chr13));
        assertThat(rightOpposite.start(), equalTo(123_456));
        assertThat(rightOpposite.id(), equalTo("bndU"));
        assertThat(rightOpposite.strand(), equalTo(Strand.POSITIVE));
        assertThat(rightOpposite.toOppositeStrand(), equalTo(variant.right()));
    }
}