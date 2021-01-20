package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

public final class DefaultVariant extends BaseVariant<DefaultVariant> {

    private DefaultVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        assertNotBreakend(alt);
        return new DefaultVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        Position end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = calculateChangeLength(ref, alt);
        return new DefaultVariant(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength);
    }

    public static DefaultVariant oneBased(Contig contig, String id, int pos, String ref, String alt) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(pos), ref, alt);
    }

    public static DefaultVariant zeroBased(Contig contig, String id, int pos, String ref, String alt) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(pos), ref, alt);
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
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, startPosition, endPosition, ref, alt, changeLength);
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
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, startPosition, endPosition, ref, alt, changeLength);
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
