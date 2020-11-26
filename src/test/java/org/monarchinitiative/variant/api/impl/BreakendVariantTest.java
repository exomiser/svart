package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

/**
 *
 * The tests use examples listed in section 5.4. of
 * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF 4.2</a> specs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class BreakendVariantTest {

    private final Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 243199373, "CM000664.1", "NC_000002.11", "chr2");
    private final Contig chr13 = Contig.of(13, "13", SequenceRole.ASSEMBLED_MOLECULE, 115169878, "CM000675.1", "NC_000013.10", "chr2");

    private final Contig chr17 = Contig.of(17, "17", SequenceRole.ASSEMBLED_MOLECULE, 83_257_441, "CM000679.2", "NC_000017.11", "chr17");

    /**
     * Breakend type {@link Strand#POSITIVE}-{@link Strand#POSITIVE}.
     *
     * <pre>13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2</pre>
     */
        private BreakendVariant tra2_bnd_U;

    /**
     * Breakend type {@link Strand#POSITIVE}-{@link Strand#NEGATIVE}.
     *
     * <pre>2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1</pre>
     */
    private BreakendVariant tra1_bnd_W;

    /**
     * Breakend type {@link Strand#NEGATIVE}-{@link Strand#NEGATIVE}.
     *
     * <pre>2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U;EVENT=tra2</pre>
     */
    private BreakendVariant tra2_bnd_V;

    /**
     * Breakend type {@link Strand#NEGATIVE}-{@link Strand#POSITIVE}.
     *
     * <pre>13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3</pre>
     */
    private BreakendVariant tra3_bnd_X;

    @BeforeEach
    public void setUp() {
        Breakend bnd_U = PartialBreakend.oneBased( chr13, "bnd_U",Strand.POSITIVE, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased( chr2, "bnd_V", Strand.POSITIVE, Position.of(321_682));
        tra2_bnd_U = new BreakendVariant("tra2", bnd_U, bnd_V, "C", "");

        Breakend bnd_W = PartialBreakend.oneBased(chr2, "bnd_W", Strand.POSITIVE, Position.of(321_681));
        Breakend bnd_Y = PartialBreakend.oneBased(chr17, "bnd_Y", Strand.POSITIVE, Position.of(198_982)).withStrand(Strand.NEGATIVE);
        tra1_bnd_W = new BreakendVariant("tra1", bnd_W, bnd_Y, "G", "");

        bnd_V = PartialBreakend.oneBased(chr2, "bnd_V", Strand.POSITIVE, Position.of(321_682)).withStrand(Strand.NEGATIVE);
        bnd_U = PartialBreakend.oneBased(chr13, "bnd_U", Strand.POSITIVE, Position.of(123_456)).withStrand(Strand.NEGATIVE);
        tra2_bnd_V = new BreakendVariant("tra2", bnd_V, bnd_U, "T", "");

        Breakend bnd_X = PartialBreakend.oneBased(chr13, "bnd_X", Strand.POSITIVE, Position.of(123_457)).withStrand(Strand.NEGATIVE);
        Breakend bnd_Z = PartialBreakend.oneBased(chr17, "bnd_Z", Strand.POSITIVE, Position.of(198_983));
        tra3_bnd_X = new BreakendVariant("tra3", bnd_X, bnd_Z, "A", "");
    }

    @Test
    public void properties() {
        assertThat(tra2_bnd_U.isSymbolic(), equalTo(true));
        assertThat(tra2_bnd_U.variantType(), equalTo(VariantType.BND));
        assertThat(tra2_bnd_U.id(), equalTo("bnd_U"));
        assertThat(tra2_bnd_U.mateId(), equalTo("bnd_V"));
        assertThat(tra2_bnd_U.eventId(), equalTo("tra2"));
    }

    @Test
    public void tra2_bnd_U() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        assertThat(tra2_bnd_U.eventId(), equalTo("tra2"));
        assertThat(tra2_bnd_U.ref(), equalTo("C"));
        assertThat(tra2_bnd_U.alt(), equalTo(""));
        assertThat(tra2_bnd_U.refLength(), equalTo(1));
        assertThat(tra2_bnd_U.length(), equalTo(0));
        assertThat(tra2_bnd_U.changeLength(), equalTo(0));

        Breakend left = tra2_bnd_U.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.position(), equalTo(Position.of(123_456)));
        assertThat(left.id(), equalTo("bnd_U"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = tra2_bnd_U.right();
        assertThat(right.contig(), equalTo(chr2));
        assertThat(right.position(), equalTo(Position.of(321_682)));
        assertThat(right.id(), equalTo("bnd_V"));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra1_bnd_W() {
        // 2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1
        assertThat(tra1_bnd_W.eventId(), equalTo("tra1"));
        assertThat(tra1_bnd_W.ref(), equalTo("G"));
        assertThat(tra1_bnd_W.alt(), equalTo(""));
        assertThat(tra1_bnd_W.refLength(), equalTo(1));
        assertThat(tra1_bnd_W.length(), equalTo(0));
        assertThat(tra1_bnd_W.changeLength(), equalTo(0));

        Breakend left = tra1_bnd_W.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.position(), equalTo(Position.of(321_681)));
        assertThat(left.id(), equalTo("bnd_W"));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = tra1_bnd_W.right();
        assertThat(right.contig(), equalTo(chr17));
        assertThat(right.position(), equalTo(Position.of(chr17.length() - 198_982 + 1)));
        assertThat(right.id(), equalTo("bnd_Y"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra2_bnd_V() {
        // 2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U;EVENT=tra2
        assertThat(tra2_bnd_V.eventId(), equalTo("tra2"));
        assertThat(tra2_bnd_V.ref(), equalTo("T"));
        assertThat(tra2_bnd_V.alt(), equalTo(""));
        assertThat(tra2_bnd_V.refLength(), equalTo(1));
        assertThat(tra2_bnd_V.length(), equalTo(0));
        assertThat(tra2_bnd_V.changeLength(), equalTo(0));

        Breakend left = tra2_bnd_V.left();
        assertThat(left.contig(), equalTo(chr2));
        assertThat(left.position(), equalTo(Position.of(chr2.length() - 321_682 + 1)));
        assertThat(left.id(), equalTo("bnd_V"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = tra2_bnd_V.right();
        assertThat(right.contig(), equalTo(chr13));
        assertThat(right.position(), equalTo(Position.of(chr13.length() - 123_456 + 1)));
        assertThat(right.id(), equalTo("bnd_U"));
        assertThat(right.strand(), equalTo(Strand.NEGATIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void tra3_bnd_X() {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        assertThat(tra3_bnd_X.eventId(), equalTo("tra3"));
        assertThat(tra3_bnd_X.ref(), equalTo("A"));
        assertThat(tra3_bnd_X.alt(), equalTo(""));
        assertThat(tra3_bnd_X.refLength(), equalTo(1));
        assertThat(tra3_bnd_X.length(), equalTo(0));
        assertThat(tra3_bnd_X.changeLength(), equalTo(0));

        Breakend left = tra3_bnd_X.left();
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.position(), equalTo(Position.of(chr13.length() - 123_457 + 1)));
        assertThat(left.id(), equalTo("bnd_X"));
        assertThat(left.strand(), equalTo(Strand.NEGATIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend right = tra3_bnd_X.right();
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
        assertThat(tra3_bnd_X.withStrand(Strand.NEGATIVE), sameInstance(tra3_bnd_X));

        BreakendVariant breakendVariant = tra3_bnd_X.withStrand(Strand.POSITIVE);
        assertThat(breakendVariant.eventId(), equalTo("tra3"));
        assertThat(breakendVariant.ref(), equalTo("A")); // stays the same!!
        assertThat(breakendVariant.alt(), equalTo(""));
        assertThat(breakendVariant.refLength(), equalTo(1));
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

    @Test
    public void withCoordinateSystem() {
        assertThat(tra3_bnd_X.withCoordinateSystem(CoordinateSystem.ONE_BASED), sameInstance(tra3_bnd_X));

        BreakendVariant breakendVariant = tra3_bnd_X.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(breakendVariant.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        Breakend left = breakendVariant.left();
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        Breakend right = breakendVariant.right();
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @Test
    public void matedBreakend() {
        // 2       4150333 28382_1 N       N]13:33423926]  284.1   PASS    SVTYPE=BND;POS=4150333;STRANDS=++:31;CIPOS=-8,10;CIEND=-2,1;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_2;EVENT=28382;
        Breakend left = PartialBreakend.oneBased(chr2, "28382_1", Strand.POSITIVE, Position.of(4150333, -8, 10));
        Breakend right = PartialBreakend.oneBased(chr13, "28382_2", Strand.POSITIVE, Position.of(33423926, -2, 1));
        BreakendVariant instance = new BreakendVariant("28382", left, right, "N", "N");
        assertThat(instance.contig(), equalTo(chr2));
        assertThat(instance.startPosition().pos(), equalTo(4150333));
        assertThat(instance.ref(), equalTo("N"));
        assertThat(instance.alt(), equalTo("N"));
        assertThat(instance.left().contig(), equalTo(chr2));
        assertThat(instance.left().pos(), equalTo(4150333));
        assertThat(instance.right().contig(), equalTo(chr13));
        assertThat(instance.right().pos(), equalTo(33423926));

        System.out.println(instance.withStrand(Strand.NEGATIVE));
        // 13      33423926        28382_2 N       N]2:4150333]    284.1   PASS    SVTYPE=BND;POS=33423926;STRANDS=++:31;CIPOS=-2,1;CIEND=-8,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_1;EVENT=28382;
        Breakend matedLeft = PartialBreakend.oneBased(chr13, "28382_2", Strand.POSITIVE, Position.of(33423926, -2, 1));
        Breakend matedRight = PartialBreakend.oneBased(chr2, "28382_1", Strand.POSITIVE, Position.of(4150333, -8, 10));
        BreakendVariant matedInstance = new BreakendVariant("28382", matedLeft, matedRight, "N", "N");

        // these can be combined using their id and mateids to recreate the full SequenceRearrangement
    }

    @Test
    public void insertedSequence() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 2  321682  bndV  T  ]13:123456]AGTNNNNNCAT  6  PASS  SVTYPE=BND;MATEID=bndU
        // 13  123456  bndU  C  CAGTNNNNNCA[2:321682[  6  PASS  SVTYPE=BND;MATEID=bndV
        Breakend left = PartialBreakend.oneBased(chr2, "bndV", Strand.POSITIVE, Position.of(321682));
        Breakend right = PartialBreakend.oneBased(chr13, "bndU", Strand.POSITIVE, Position.of(123456));
        // TODO: parse strands and partial breakends from record

        BreakendVariant first = new BreakendVariant("", left, right, "T", "AGTNNNNNCAT");
        System.out.println(first);
        System.out.println(first.toOppositeStrand());
        BreakendVariant second = new BreakendVariant("", right, left, "C", "CAGTNNNNNCA");
    }

    @Test
    public void positiveNegativeStrand() {
        // 2       135592459       28388_1 N       N[13:36400702[  2049.07 LOW     SVTYPE=BND;POS=135592459;STRANDS=+-:125;CIPOS=-2,10;CIEND=-9,9;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_2;EVENT=28388;
        // 13      36400702        28388_2 N       ]2:135592459]N  2049.07 LOW     SVTYPE=BND;POS=36400702;STRANDS=+-:125;CIPOS=-9,9;CIEND=-2,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_1;EVENT=28388;

    }

    @Test
    public void negativeNegativeStrand() {
        // 1       10567   2838_1  N       [15:102520406[N 47911.3 LOW     SVTYPE=BND;POS=10567;STRANDS=--:97;IMPRECISE;CIPOS=-205,24;CIEND=-104,42;CIPOS95=-20,20;CIEND95=-6,6;MATEID=2838_2;EVENT=2838;
        //15      102520406       2838_2  N       [1:10567[N      47911.3 LOW     SVTYPE=BND;POS=102520406;STRANDS=--:97;IMPRECISE;CIPOS=-104,42;CIEND=-205,24;CIPOS95=-6,6;CIEND95=-20,20;MATEID=2838_1;EVENT=2838;

    }
}