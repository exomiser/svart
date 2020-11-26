package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

/**
 * The tests use examples listed in section 5.4. of
 * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF 4.2</a> specs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class BreakendVariantTest {

    // real GRCh38.p13 contig data
    private final Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 243_199_373, "CM000664.1", "NC_000002.11", "chr2");
    private final Contig chr13 = Contig.of(13, "13", SequenceRole.ASSEMBLED_MOLECULE, 115_169_878, "CM000675.1", "NC_000013.10", "chr13");
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
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682));
        tra2_bnd_U = new BreakendVariant("tra2", bnd_U, bnd_V, "C", "");

        Breakend bnd_W = PartialBreakend.oneBased("bnd_W", chr2, Strand.POSITIVE, Position.of(321_681));
        Breakend bnd_Y = PartialBreakend.oneBased("bnd_Y", chr17, Strand.POSITIVE, Position.of(198_982)).withStrand(Strand.NEGATIVE);
        tra1_bnd_W = new BreakendVariant("tra1", bnd_W, bnd_Y, "G", "");

        bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682)).withStrand(Strand.NEGATIVE);
        bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456)).withStrand(Strand.NEGATIVE);
        tra2_bnd_V = new BreakendVariant("tra2", bnd_V, bnd_U, "T", "");

        Breakend bnd_X = PartialBreakend.oneBased("bnd_X", chr13, Strand.POSITIVE, Position.of(123_457)).withStrand(Strand.NEGATIVE);
        Breakend bnd_Z = PartialBreakend.oneBased("bnd_Z", chr17, Strand.POSITIVE, Position.of(198_983));
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
    public void singleBreakend() {
        // Single breakends are breakends that are not part of a novel adjacency.
        // See section 5.4.9 of VCF v4.2 specs for more info.
        //
        // 2  321681  bnd_W   G   G.    6   PASS    SVTYPE=BND
        Breakend left = PartialBreakend.oneBased("bnd_W", chr2, Strand.POSITIVE, Position.of(321_681));
        Breakend right = Breakend.unresolved();

        BreakendVariant instance = new BreakendVariant("", left, right, "G", "");

        Breakend l = instance.left();
        assertThat(l.contig(), equalTo(chr2));
        assertThat(l.position(), equalTo(Position.of(321_681)));
        assertThat(l.id(), equalTo("bnd_W"));
        assertThat(l.strand(), equalTo(Strand.POSITIVE));
        assertThat(l.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        Breakend r = instance.right();
        assertThat(r.isUnresolved(), equalTo(true));

        // 13  123457  bnd_X   A   .A    6   PASS    SVTYPE=BND
        left = Breakend.unresolved();
        right = PartialBreakend.oneBased("bnd_X", chr13, Strand.POSITIVE, Position.of(123_457));

        instance = new BreakendVariant("", left, right, "A", "");
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
//        assertThat(variant.withStrand(Strand.UNKNOWN), sameInstance(variant));
//        assertThat(variant.withStrand(Strand.UNSTRANDED), sameInstance(variant));

        variant = variant.withStrand(Strand.NEGATIVE);
        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("C")); // stays the same!!
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
//        assertThat(variant.withStrand(Strand.UNKNOWN), sameInstance(variant));
//        assertThat(variant.withStrand(Strand.UNSTRANDED), sameInstance(variant));

        variant = variant.withStrand(Strand.POSITIVE);
        assertThat(variant.eventId(), equalTo(""));
        assertThat(variant.ref(), equalTo("T")); // stays the same!!
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