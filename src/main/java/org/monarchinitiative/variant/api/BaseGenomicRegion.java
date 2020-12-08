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
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);
        if (coordinateSystem == CoordinateSystem.ZERO_BASED && startPosition.pos() >= endPosition.pos()) {
            throw new IllegalArgumentException("Zero-based region must have a start position greater than the end position");
        }
        if (coordinateSystem == CoordinateSystem.ONE_BASED && startPosition.pos() > endPosition.pos()) {
            throw new IllegalArgumentException("One-based region must have a start position greater than or equal to the end position");
        }
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
                "contig=" + contig +
                ", strand=" + strand +
                ", coordinateSystem=" + coordinateSystem +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                '}';
    }
}
