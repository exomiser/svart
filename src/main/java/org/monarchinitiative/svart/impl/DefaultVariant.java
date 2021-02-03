package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

public final class DefaultVariant extends BaseVariant<DefaultVariant> {

    private DefaultVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    public DefaultVariant(Builder builder) {
        super(builder);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        VariantType.requireNonBreakend(alt);
        return new DefaultVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    public static DefaultVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        Position end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = calculateChangeLength(ref, alt);
        return new DefaultVariant(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength);
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
