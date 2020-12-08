package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

public final class DefaultVariant extends BaseVariant<DefaultVariant> {


    private DefaultVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        if (VariantType.isBreakend(alt)) {
            throw new IllegalArgumentException("Unable to create variant from breakend allele " + alt);
        }
        return new DefaultVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }


    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        Position end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = alt.length() - ref.length();
        return new DefaultVariant(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength);
    }

    private static Position calculateEnd(Position start, CoordinateSystem coordinateSystem, String ref, String alt) {
        // SNV case
        if ((ref.length() | alt.length()) == 1) {
            return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.shift(1) : start;
        }
        return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.withPos(start.pos() + ref.length()) : start.withPos(start.pos() + ref.length() - 1);
    }

    public static DefaultVariant oneBased(Contig contig, String id, int pos, String ref, String alt) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos), ref, alt);
    }

    public static DefaultVariant zeroBased(Contig contig, String id, int pos, String ref, String alt) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(pos), ref, alt);
    }

    public static DefaultVariant oneBased(Contig contig, int pos, String ref, String alt) {
        return oneBased(contig, "", pos, ref, alt);
    }

    public static DefaultVariant zeroBased(Contig contig, int pos, String ref, String alt) {
        return zeroBased(contig, "", pos, ref, alt);
    }

    // symbolic constructors
    /**
     * @return precise one-based, positive strand symbolic variant
     */
    public static DefaultVariant oneBased(Contig contig, int start, int end, String ref, String alt, int changeLength) {
        return oneBased(contig, "", Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return precise one-based, positive strand symbolic variant
     */
    public static DefaultVariant oneBased(Contig contig, String id, int start, int end, String ref, String alt, int changeLength) {
        return oneBased(contig, id, Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return one-based, positive strand symbolic variant
     */
    public static DefaultVariant oneBased(Contig contig, String id, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ONE_BASED, startPosition, endPosition, ref, alt, changeLength);
    }

    /**
     * @return precise zero-based, positive strand symbolic variant
     */
    public static DefaultVariant zeroBased(Contig contig, int start, int end, String ref, String alt, int changeLength) {
        return zeroBased(contig, "", Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return precise zero-based, positive strand symbolic variant
     */
    public static DefaultVariant zeroBased(Contig contig, String id, int start, int end, String ref, String alt, int changeLength) {
        return zeroBased(contig, id, Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    public static DefaultVariant zeroBased(Contig contig, String id, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, startPosition, endPosition, ref, alt, changeLength);
    }

    @Override
    protected DefaultVariant newVariantInstance(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return new DefaultVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Variant{" +
                "contig=" + contig().id() +
                ", strand=" + strand() +
                ", coordinateSystem=" + coordinateSystem() +
                ", startPosition=" + startPosition() +
                ", endPosition=" + endPosition() +
                ", ref='" + ref() + '\'' +
                ", alt='" + alt() + '\'' +
                ", variantType=" + variantType() +
                ", length=" + length() +
                ", changeLength=" + changeLength() +
                '}';
    }
}
