package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Implementation of a structural variant that involves two different contigs.
 * <p>
 * Note: {@link BreakendVariant#ref} field is not altered during strand conversion.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class BreakendVariant implements Variant, Breakended {

    private final String eventId;

    private final Breakend left;

    private final Breakend right;

    private final String ref;

    /**
     * String representing the inserted sequence without the <em>ref</em> allele, as described in Section 5.4.1 of the
     * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF v4.2 specification</a>
     */
    private final String alt;

    // Accept the standard VCF input fields, including mateId and eventId

    // 1       28681758        gnomAD-SV_v2.1_BND_1_1117       N       <BND>   965     UNRESOLVED      END=28681759;SVTYPE=BND;SVLEN=1725;CHR2=1;POS2=28683483;END2=28683484;ALGORITHMS=delly,manta;EVIDENCE=PE,SR;UNRESOLVED_TYPE=SINGLE_ENDER_-+;
    //
    // 2       4150333 28382_1 N       N]13:33423926]  284.1   PASS    SVTYPE=BND;POS=4150333;STRANDS=++:31;CIPOS=-8,10;CIEND=-2,1;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_2;EVENT=28382;
    // 13      33423926        28382_2 N       N]2:4150333]    284.1   PASS    SVTYPE=BND;POS=33423926;STRANDS=++:31;CIPOS=-2,1;CIEND=-8,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_1;EVENT=28382;
    // 2       135592459       28388_1 N       N[13:36400702[  2049.07 LOW     SVTYPE=BND;POS=135592459;STRANDS=+-:125;CIPOS=-2,10;CIEND=-9,9;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_2;EVENT=28388;
    // 13      36400702        28388_2 N       ]2:135592459]N  2049.07 LOW     SVTYPE=BND;POS=36400702;STRANDS=+-:125;CIPOS=-9,9;CIEND=-2,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_1;EVENT=28388;
    // 1       10567   2838_1  N       [15:102520406[N 47911.3 LOW     SVTYPE=BND;POS=10567;STRANDS=--:97;IMPRECISE;CIPOS=-205,24;CIEND=-104,42;CIPOS95=-20,20;CIEND95=-6,6;MATEID=2838_2;EVENT=2838;
    //15      102520406       2838_2  N       [1:10567[N      47911.3 LOW     SVTYPE=BND;POS=102520406;STRANDS=--:97;IMPRECISE;CIPOS=-104,42;CIEND=-205,24;CIPOS95=-6,6;CIEND95=-20,20;MATEID=2838_1;EVENT=2838;

    public BreakendVariant(String eventId,
                           Breakend left,
                           Breakend right,
                           String ref,
                           String alt) {
        this.eventId = Objects.requireNonNull(eventId, "Event ID must not be null");
        this.left = Objects.requireNonNull(left, "Left breakend cannot be null");
        this.right = Objects.requireNonNull(right, "Right breakend cannot be null");
        this.ref = Objects.requireNonNull(ref, "Ref sequence cannot be null");
        this.alt = Objects.requireNonNull(alt, "Alt sequence cannot be null");
    }

    /**
     * @return id of the breakend
     */
    @Override
    public String id() {
        return left.id();
    }

    /**
     * @return event id of the breakend variant
     */
    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Breakend left() {
        return left;
    }

    @Override
    public Breakend right() {
        return right;
    }

    @Override
    public Contig contig() {
        return left.contig();
    }

    @Override
    public Position startPosition() {
        return left.position();
    }

    @Override
    public Position endPosition() {
        return left.position();
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return left.coordinateSystem();
    }

    @Override
    public BreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (left.coordinateSystem() == coordinateSystem) {
            return this;
        }
        return new BreakendVariant(eventId, left.withCoordinateSystem(coordinateSystem), right.withCoordinateSystem(coordinateSystem), ref, alt);
    }

    /**
     * @return strand of the left breakend
     */
    @Override
    public Strand strand() {
        return left.strand();
    }

    public BreakendVariant withStrand(Strand strand) {
        if (strand().hasComplement() && strand() != strand) {
            Breakend l = right.toOppositeStrand();
            Breakend r = left.toOppositeStrand();
            return new BreakendVariant(eventId, l, r, ref, Seq.reverseComplement(alt));
        }
        return this;
    }

    @Override
    public BreakendVariant toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    @Override
    public int length() {
        return alt.length();
    }

    @Override
    public int refLength() {
        return ref.length();
    }

    /**
     * @return always <code>0</code>
     */
    @Override
    public int changeLength() {
        return 0;
    }

    /**
     * @return reference allele on {@link Strand#POSITIVE}
     */
    @Override
    public String ref() {
        return ref;
    }

    /**
     * @return string with inserted sequence, as observed in <em>ALT</em> allele. The allele is trimmed to remove the
     * bases shared with the <em>REF</em> allele
     */
    @Override
    public String alt() {
        return alt;
    }

    /**
     * @return always {@link VariantType#BND}
     */
    @Override
    public VariantType variantType() {
        return VariantType.BND;
    }

    /**
     * @return always <code>true</code>
     */
    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BreakendVariant)) return false;
        BreakendVariant that = (BreakendVariant) o;
        return eventId.equals(that.eventId) &&
                left.equals(that.left) &&
                right.equals(that.right) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, left, right, ref, alt);
    }

    @Override
    public String toString() {
        return "BV(" + eventId + ')' +
                "[" + left +
                ", " + right +
                "] " + alt;
    }
}
