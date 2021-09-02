package org.monarchinitiative.svart;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public abstract class BaseGenomicRegion<T extends GenomicRegion> implements GenomicRegion {

    private final Contig contig;
    private final Strand strand;
    private final Coordinates coordinates;

    protected BaseGenomicRegion(Contig contig, Strand strand, Coordinates coordinates) {
        this.contig = Objects.requireNonNull(contig, "contig must not be null");
        this.strand = Objects.requireNonNull(strand, "strand must not be null");
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
        coordinates.validateOnContig(contig);
    }

    protected BaseGenomicRegion(Builder<?> builder) {
        this(builder.contig, builder.strand, builder.coordinates);
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    public Coordinates coordinates() {
        return coordinates;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withCoordinateSystem(CoordinateSystem requiredCoordinateSystem) {
        if (this.coordinateSystem() == requiredCoordinateSystem) {
            return (T) this;
        }
        return newRegionInstance(contig, strand, coordinates.withCoordinateSystem(requiredCoordinateSystem));
    }

    @Override
    public T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    @Override
    public T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withStrand(Strand strand) {
        if (this.strand == strand) {
            return (T) this;
        }
        return newRegionInstance(contig, strand, coordinates.invert(contig));
    }

    @Override
    public T toOppositeStrand() {
        return withStrand(strand.opposite());
    }

    /**
     * Hook for classes extending this base class to provide a way for the {@link BaseGenomicRegion} to provide the results
     * of a withStrand() or withCoordinateSystem() without the extending classes having to implement {@link CoordinateSystemed}
     * or {@link Stranded} themselves and for the specialised type to be returned.
     *
     * @param contig
     * @param strand
     * @param coordinates
     * @return
     */
    protected abstract T newRegionInstance(Contig contig, Strand strand, Coordinates coordinates);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseGenomicRegion)) return false;
        BaseGenomicRegion<?> that = (BaseGenomicRegion<?>) o;
        return contig.equals(that.contig) &&
                strand == that.strand &&
                coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, coordinates);
    }

    @Override
    public String toString() {
        if (!coordinates.isPrecise()) {
            return "BaseGenomicRegion{" +
                    "contig=" + contig.id() +
                    ", strand=" + strand +
                    ", coordinateSystem=" + coordinateSystem() +
                    ", start=" + coordinates.start() + " " + coordinates.startConfidenceInterval() +
                    ", end=" + coordinates.end() + " " + coordinates.endConfidenceInterval() +
                    '}';
        }
        return "BaseGenomicRegion{" +
                "contig=" + contig.id() +
                ", strand=" + strand +
                ", coordinateSystem=" + coordinateSystem() +
                ", start=" + coordinates.start() +
                ", end=" + coordinates.end() +
                '}';
    }

    protected abstract static class Builder<T extends Builder<T>> {

        private static final Coordinates DEFAULT_COORDINATES = Coordinates.of(CoordinateSystem.FULLY_CLOSED, 1, 1);

        protected Contig contig;
        // we're primarily interested in VCF coordinates so we're defaulting to 1-based coordinates on the + strand
        protected Strand strand = Strand.POSITIVE;
        protected Coordinates coordinates = DEFAULT_COORDINATES;

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters so as
        // to maintain the correct state when finally built.

        public T with(GenomicRegion genomicRegion) {
            Objects.requireNonNull(genomicRegion, "genomicRegion cannot be null");
            return with(genomicRegion.contig(), genomicRegion.strand(), genomicRegion.coordinates());
        }

        public T with(Variant variant) {
            Objects.requireNonNull(variant, "variant cannot be null");
            return with(variant.contig(), variant.strand(), variant.coordinates());
        }

        public T with(Contig contig, Strand strand, Coordinates coordinates) {
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            this.coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
            return self();
        }

        public T asZeroBased() {
            return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
        }

        public T asOneBased() {
            return withCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
        }

        public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
            this.coordinates = coordinates.withCoordinateSystem(coordinateSystem);
            return self();
        }

        public T onPositiveStrand() {
            return withStrand(Strand.POSITIVE);
        }

        public T onNegativeStrand() {
            return withStrand(Strand.NEGATIVE);
        }

        public T withStrand(Strand strand) {
            if (this.strand == strand) {
                return self();
            }
            this.strand = strand;
            coordinates = coordinates.invert(contig);
            return self();
        }

        protected abstract BaseGenomicRegion<?> build();

        protected abstract T self();
    }
}
