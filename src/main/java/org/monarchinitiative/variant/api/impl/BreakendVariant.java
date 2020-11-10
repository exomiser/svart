package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class BreakendVariant implements Variant, Breakended {

    private final String id;
    private final String mateId;
    private final String eventId;
    private final Breakend left;
    private final Breakend right;
    private final String alt;
    private final VariantType variantType;

    // Accept the standard VCF input fields, including mateId and eventId

    // 1       28681758        gnomAD-SV_v2.1_BND_1_1117       N       <BND>   965     UNRESOLVED      END=28681759;SVTYPE=BND;SVLEN=1725;CHR2=1;POS2=28683483;END2=28683484;ALGORITHMS=delly,manta;EVIDENCE=PE,SR;UNRESOLVED_TYPE=SINGLE_ENDER_-+;

    // 2       4150333 28382_1 N       N]13:33423926]  284.1   PASS    SVTYPE=BND;POS=4150333;STRANDS=++:31;CIPOS=-8,10;CIEND=-2,1;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_2;EVENT=28382;
    // 13      33423926        28382_2 N       N]2:4150333]    284.1   PASS    SVTYPE=BND;POS=33423926;STRANDS=++:31;CIPOS=-2,1;CIEND=-8,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_1;EVENT=28382;
    // 2       135592459       28388_1 N       N[13:36400702[  2049.07 LOW     SVTYPE=BND;POS=135592459;STRANDS=+-:125;CIPOS=-2,10;CIEND=-9,9;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_2;EVENT=28388;
    // 13      36400702        28388_2 N       ]2:135592459]N  2049.07 LOW     SVTYPE=BND;POS=36400702;STRANDS=+-:125;CIPOS=-9,9;CIEND=-2,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_1;EVENT=28388;
    // 1       10567   2838_1  N       [15:102520406[N 47911.3 LOW     SVTYPE=BND;POS=10567;STRANDS=--:97;IMPRECISE;CIPOS=-205,24;CIEND=-104,42;CIPOS95=-20,20;CIEND95=-6,6;MATEID=2838_2;EVENT=2838;
    //15      102520406       2838_2  N       [1:10567[N      47911.3 LOW     SVTYPE=BND;POS=102520406;STRANDS=--:97;IMPRECISE;CIPOS=-104,42;CIEND=-205,24;CIPOS95=-6,6;CIEND95=-20,20;MATEID=2838_1;EVENT=2838;


    public BreakendVariant(String id, String mateId, String eventId, Breakend left, Breakend right, String alt, VariantType variantType) {
        this.id = id;
        this.mateId = mateId;
        this.eventId = eventId;
        this.left = left;
        this.right = right;
        this.alt = alt;
        this.variantType = variantType;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getMateId() {
        return mateId;
    }

    public String getEventId() {
        return eventId;
    }

    @Override
    public Contig getContig() {
        return left.getContig();
    }

    @Override
    public Position getStartPosition() {
        return left.getPosition();
    }

    @Override
    public Position getEndPosition() {
        return left.getPosition();
    }

    @Override
    public int getLength() {
        return alt.length();
    }

    @Override
    public String getRef() {
        return left.getRef();
    }

    @Override
    public String getAlt() {
        return alt;
    }

    @Override
    public VariantType getType() {
        return variantType;
    }

    public Breakend getLeft() {
        return left;
    }

    public Breakend getRight() {
        return right;
    }

    @Override
    public Strand getStrand() {
        return left.getStrand();
    }

    public BreakendVariant withStrand(Strand strand) {
        if (left.getStrand().equals(strand)) {
            return this;
        } else {
            return new BreakendVariant(id, mateId, eventId, right.toOppositeStrand(), left.toOppositeStrand(), Seq
                    .reverseComplement(alt), variantType);
        }
    }

    @Override
    public BreakendVariant toOppositeStrand() {
        return null;
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BreakendVariant)) return false;
        BreakendVariant that = (BreakendVariant) o;
        return id.equals(that.id) &&
                mateId.equals(that.mateId) &&
                eventId.equals(that.eventId) &&
                left.equals(that.left) &&
                right.equals(that.right) &&
                alt.equals(that.alt) &&
                variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mateId, eventId, left, right, alt, variantType);
    }

    @Override
    public String toString() {
        return "BreakendVariant{" +
                "id='" + id + '\'' +
                ", mateId='" + mateId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", left=" + left +
                ", right=" + right +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                '}';
    }
}
