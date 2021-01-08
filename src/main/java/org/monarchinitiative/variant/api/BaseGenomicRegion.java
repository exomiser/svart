package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public abstract class BaseGenomicRegion<T extends GenomicRegion> implements GenomicRegion {

    private final Contig contig;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position startPosition;
    private final Position endPosition;

    protected BaseGenomicRegion(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        this.contig = Objects.requireNonNull(contig, "contig must not be null");
        this.strand = Objects.requireNonNull(strand, "strand must not be null");
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem, "coordinateSystem must not be null");
        this.startPosition = Objects.requireNonNull(startPosition, "startPosition must not be null");
        this.endPosition = Objects.requireNonNull(endPosition, "endPosition must not be null");
        if (coordinateSystem == CoordinateSystem.ZERO_BASED && startPosition.pos() >= endPosition.pos()) {
            throw new IllegalArgumentException("Zero-based region " + contig.name() + "-" + startPosition.pos() + "-" + endPosition.pos() + " must have a start position before the end position");
        }
        if (coordinateSystem == CoordinateSystem.ONE_BASED && startPosition.pos() > endPosition.pos()) {
            throw new IllegalArgumentException("One-based region " + contig.name() + "-" + startPosition.pos() + "-" + endPosition.pos() + " must have a start position before or same as the end position");
        }
    }

    protected BaseGenomicRegion(Builder<?> builder) {
        this(builder.contig, builder.strand, builder.coordinateSystem, builder.start, builder.end);
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Position startPosition() {
        return startPosition;
    }

    @Override
    public Position endPosition() {
        return endPosition;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withCoordinateSystem(CoordinateSystem requiredCoordinateSystem) {
        if (this.coordinateSystem == requiredCoordinateSystem) {
            return (T) this;
        }
        return newRegionInstance(contig, strand, requiredCoordinateSystem, normalisedStartPosition(requiredCoordinateSystem), endPosition);
    }

    @Override
    public T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    public T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withStrand(Strand strand) {
        if (this.strand == strand) {
            return (T) this;
        }
        Position start = startPosition.invert(contig, coordinateSystem());
        Position end = endPosition.invert(contig, coordinateSystem());
        return newRegionInstance(contig, strand, coordinateSystem, end, start);
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
     * @param coordinateSystem
     * @param startPosition
     * @param endPosition
     * @return
     */
    protected abstract T newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseGenomicRegion)) return false;
        BaseGenomicRegion<?> that = (BaseGenomicRegion<?>) o;
        return contig.equals(that.contig) &&
                strand == that.strand &&
                coordinateSystem == that.coordinateSystem &&
                startPosition.equals(that.startPosition) &&
                endPosition.equals(that.endPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    @Override
    public String toString() {
        return "BaseGenomicRegion{" +
                "contig=" + contig.id() +
                ", strand=" + strand +
                ", coordinateSystem=" + coordinateSystem +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                '}';
    }

    protected abstract static class Builder<T extends Builder<T>> {

        protected Contig contig;
        // we're primarily interested in VCF coordinates so we're defaulting to 1-based coordinates on the + strand
        protected Strand strand = Strand.POSITIVE;
        protected CoordinateSystem coordinateSystem = CoordinateSystem.ONE_BASED;
        protected Position start = Position.of(1);
        protected Position end = start;

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters so as
        // to maintain the correct state when finally built.

        public T with(GenomicRegion genomicRegion) {
            Objects.requireNonNull(genomicRegion, "genomicRegion cannot be null");
            return with(genomicRegion.contig(), genomicRegion.strand(), genomicRegion.coordinateSystem(), genomicRegion.startPosition(), genomicRegion.endPosition());
        }

        public T with(Variant variant) {
            Objects.requireNonNull(variant, "variant cannot be null");
            return with(variant.contig(), variant.strand(), variant.coordinateSystem(), variant.startPosition(), variant.endPosition());
        }

        public T with(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            this.coordinateSystem = Objects.requireNonNull(coordinateSystem, "coordinateSystem must not be null");
            this.start = Objects.requireNonNull(startPosition, "startPosition must not be null");
            this.end = Objects.requireNonNull(endPosition, "endPosition must not be null");
            return self();
        }

        public T asZeroBased() {
            return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        }

        public T asOneBased() {
            return withCoordinateSystem(CoordinateSystem.ONE_BASED);
        }

        public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
            if (this.coordinateSystem == coordinateSystem) {
                return self();
            }
            start = start.shift(this.coordinateSystem.startDelta(coordinateSystem));
            this.coordinateSystem = coordinateSystem;
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
            Position invertedStart = start.invert(contig, coordinateSystem);
            start = end.invert(contig, coordinateSystem);
            end = invertedStart;
            return self();
        }

        protected abstract GenomicRegion build();

        protected abstract T self();
    }
}
