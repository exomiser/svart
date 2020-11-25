package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * 0- or 1-based region e.g. half-open [a,b) or fully closed [a,b] as indicated by the {@link CoordinateSystem}
 */
class ContigRegion implements GenomicRegion {

    private final Contig contig;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position startPosition;
    private final Position endPosition;

    private final int startZeroBased;

    private ContigRegion(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.coordinateSystem = coordinateSystem;
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);
        this.startZeroBased = coordinateSystem == CoordinateSystem.ZERO_BASED ? startPosition.pos() : startPosition.pos() - 1;
    }

    public static ContigRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return new ContigRegion(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    public static ContigRegion oneBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return new ContigRegion(contig, strand, CoordinateSystem.ONE_BASED, startPosition, endPosition);
    }

    public static ContigRegion zeroBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return new ContigRegion(contig, strand, CoordinateSystem.ZERO_BASED, startPosition, endPosition);
    }


    @Override
    public Contig contig() {
        return contig;
    }


    @Override
    public int startZeroBased() {
        return startZeroBased;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public ContigRegion withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        int startDelta = this.coordinateSystem.delta(coordinateSystem);
        return new ContigRegion(contig, strand, coordinateSystem, startPosition.shiftPos(startDelta), endPosition);
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
    public Strand strand() {
        return strand;
    }

    @Override
    public ContigRegion withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else if (coordinateSystem.isOneBased()) {
            Position start = Position.of(contig.length() - start() + 1, startPosition.confidenceInterval().toOppositeStrand());
            Position end = Position.of(contig.length() - end() + 1, endPosition.confidenceInterval().toOppositeStrand());
            return new ContigRegion(contig, strand, coordinateSystem, end, start);
        }
        Position start = Position.of(contig.length() - start(), startPosition.confidenceInterval().toOppositeStrand());
        Position end = Position.of(contig.length() - end(), endPosition.confidenceInterval().toOppositeStrand());
        return new ContigRegion(contig, strand, coordinateSystem, end, start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContigRegion)) return false;
        ContigRegion that = (ContigRegion) o;
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
        return "ContigRegion{" +
                "contig=" + contig.id() +
                ", strand=" + strand +
                ", coordinateSystem=" + coordinateSystem +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                '}';
    }
}
