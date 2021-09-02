package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

public final class DefaultVariant extends BaseVariant<DefaultVariant> {

    private DefaultVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public DefaultVariant(Builder builder) {
        super(builder);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        VariantType.requireNonBreakend(alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, startPosition.pos(), startPosition.confidenceInterval(), endPosition.pos(), endPosition.confidenceInterval());
        return new DefaultVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        Position end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = calculateChangeLength(ref, alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start.pos(), start.confidenceInterval(), end.pos(), end.confidenceInterval());
        return new DefaultVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    @Override
    protected DefaultVariant newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return new DefaultVariant(contig, id, strand, coordinates, ref, alt, changeLength);
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
                ", startPosition=" + start() +
                ", endPosition=" + end() +
                ", ref='" + ref() + '\'' +
                ", alt='" + alt() + '\'' +
                ", variantType=" + variantType() +
                ", length=" + length() +
                ", changeLength=" + changeLength() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseVariant.Builder<Builder> {

        @Override
        public DefaultVariant build() {
            return new DefaultVariant(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
