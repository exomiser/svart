package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

public final class DefaultGenomicVariant extends BaseGenomicVariant<DefaultGenomicVariant> implements Comparable<GenomicVariant> {

    private DefaultGenomicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public DefaultGenomicVariant(Builder builder) {
        super(builder);
    }

    // symbolic variants
    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        VariantType.requireNonBreakend(alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        VariantType.requireNonBreakend(alt);
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    // sequence variants
    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        int end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = calculateChangeLength(ref, alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        int changeLength = calculateChangeLength(ref, alt);
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    @Override
    protected DefaultGenomicVariant newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength);
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
                "contig=" + contigId() +
                ", id='" + id() + '\'' +
                ", strand=" + strand() +
                ", " + formatCoordinates() +
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

    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
    }

    public static class Builder extends BaseGenomicVariant.Builder<Builder> {

        @Override
        public DefaultGenomicVariant build() {
            return new DefaultGenomicVariant(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
