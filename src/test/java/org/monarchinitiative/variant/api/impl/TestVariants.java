package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

/**
 * Class with examples of structural variants as described in <a href="https://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF specs v4.2</a>.
 */
public class TestVariants {

    // real GRCh38.p13 contig data
    public static final Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 243_199_373, "CM000664.1", "NC_000002.11", "chr2");
    public static final Contig chr13 = Contig.of(13, "13", SequenceRole.ASSEMBLED_MOLECULE, 115_169_878, "CM000675.1", "NC_000013.10", "chr13");
    public static final Contig chr17 = Contig.of(17, "17", SequenceRole.ASSEMBLED_MOLECULE, 83_257_441, "CM000679.2", "NC_000017.11", "chr17");

    private TestVariants() {
        // static utility class
    }

    /**
     * Make variant representing event consisting of <em>bnd_U</em> and <em>bnd_V</em> described in section 5.4 of VCF specs.
     * <p>
     * The variant joins <em>chr13</em> {@link Strand#POSITIVE} to <em>chr2</em> {@link Strand#POSITIVE}.
     * <p>
     * <pre>13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2</pre>
     */
    public static BreakendVariant bnd_U() {
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682));
        return new BreakendVariant("tra2", bnd_U, bnd_V, "C", "");
    }

    /**
     * Make variant representing event consisting of <em>bnd_W</em> and <em>bnd_Y</em> described in section 5.4 of VCF specs.
     * <p>
     * The variant joins <em>chr2</em> {@link Strand#POSITIVE} to <em>chr17</em> {@link Strand#NEGATIVE}.
     * <p>
     * <pre>2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1</pre>
     */
    public static BreakendVariant bnd_W() {
        Breakend bnd_W = PartialBreakend.oneBased("bnd_W", chr2, Strand.POSITIVE, Position.of(321_681));
        Breakend bnd_Y = PartialBreakend.oneBased("bnd_Y", chr17, Strand.POSITIVE, Position.of(198_982)).withStrand(Strand.NEGATIVE);
        return new BreakendVariant("tra1", bnd_W, bnd_Y, "G", "");
    }

    /**
     * Make variant representing event consisting of <em>bnd_V</em> and <em>bnd_U</em> described in section 5.4 of VCF specs.
     * <p>
     * The variant joins <em>chr2</em> {@link Strand#NEGATIVE} to <em>chr13</em> {@link Strand#NEGATIVE}.
     * <p>
     * <pre>2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U;EVENT=tra2</pre>
     */
    public static BreakendVariant bnd_V() {
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682)).withStrand(Strand.NEGATIVE);
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456)).withStrand(Strand.NEGATIVE);
        return new BreakendVariant("tra2", bnd_V, bnd_U, "T", "");
    }


    /**
     * Make variant representing event consisting of <em>bnd_X</em> and <em>bnd_Z</em> described in section 5.4 of VCF specs.
     * <p>
     * The variant joins <em>chr13</em> {@link Strand#NEGATIVE} to <em>chr17</em> {@link Strand#POSITIVE}.
     * <p>
     * <pre>13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3</pre>
     */
    public static BreakendVariant bnd_X() {
        Breakend bnd_X = PartialBreakend.oneBased("bnd_X", chr13, Strand.POSITIVE, Position.of(123_457)).withStrand(Strand.NEGATIVE);
        Breakend bnd_Z = PartialBreakend.oneBased("bnd_Z", chr17, Strand.POSITIVE, Position.of(198_983));
        return new BreakendVariant("tra3", bnd_X, bnd_Z, "A", "");
    }

    /**
     * Make variant representing event consisting of <em>bnd_U</em> and <em>bnd_V</em> described in section 5.4.1 of VCF specs.
     * <p>
     * The variant joins <em>chr13</em> {@link Strand#POSITIVE} to <em>chr2</em> {@link Strand#POSITIVE}.
     * <p>
     * <pre>13  123456  bnd_U   C   CAGTNNNNNCA[2:321682[    6   PASS    SVTYPE=BND;MATEID=bnd_V;EVENT=tra2</pre>
     */
    public static BreakendVariant bnd_U_withInserted() {
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682));
        return new BreakendVariant("tra2", bnd_U, bnd_V, "C", "AGTNNNNNCA");
    }

    /**
     * Make variant representing event consisting of <em>bnd_X</em> and <em>bnd_Z</em> described in section 5.4.1 of VCF specs.
     * <p>
     * The variant joins <em>chr2</em> {@link Strand#NEGATIVE} to <em>chr13</em> {@link Strand#NEGATIVE}.
     * <p>
     * <pre>2  321582  bnd_V   T   ]13:123456]AGTNNNNNCAT    6   PASS    SVTYPE=BND;MATEID=bnd_U;EVENT=tra2</pre>
     */
    public static BreakendVariant bnd_V_withInserted() {
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", chr2, Strand.POSITIVE, Position.of(321_682)).withStrand(Strand.NEGATIVE);
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", chr13, Strand.POSITIVE, Position.of(123_456)).withStrand(Strand.NEGATIVE);
        return new BreakendVariant("tra2", bnd_V, bnd_U, "T", "TGNNNNNACT");
    }

    /**
     * Make breakend variant where left breakend is unresolved.
     * <p>
     * <pre>13  123457  bnd_X   A   .A    6   PASS    SVTYPE=BND</pre>
     * <p>
     * See section 5.4.9 of VCF v4.2 specs for more info.
     */
    public static BreakendVariant bnd_X_leftUnresolved() {
        Breakend left = Breakend.unresolved();
        Breakend right = PartialBreakend.oneBased("bnd_X", chr13, Strand.POSITIVE, Position.of(123_457));

        return new BreakendVariant("", left, right, "A", "");
    }

    /**
     * Make breakend variant where right breakend is unresolved.
     * <p>
     * <pre>2  321681  bnd_W   G   G.    6   PASS    SVTYPE=BND</pre>
     * <p>
     * See section 5.4.9 of VCF v4.2 specs for more info.
     */
    public static BreakendVariant bnd_W_rightUnresolved() {
        Breakend left = PartialBreakend.oneBased("bnd_W", chr2, Strand.POSITIVE, Position.of(321_681));
        Breakend right = Breakend.unresolved();

        return new BreakendVariant("", left, right, "G", "");
    }
}
