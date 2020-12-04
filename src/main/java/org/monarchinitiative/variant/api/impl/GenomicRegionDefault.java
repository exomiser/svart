package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * 0- or 1-based region e.g. half-open [a,b) or fully closed [a,b] as indicated by the {@link CoordinateSystem}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class GenomicRegionDefault implements GenomicRegion {

    private final Contig contig;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position startPosition;
    private final Position endPosition;

    private final int startZeroBased;

    private GenomicRegionDefault(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.coordinateSystem = coordinateSystem;
        this.startPosition = checkNotBeyondContigCoordinates(startPosition, coordinateSystem, contig.length());
        this.endPosition = checkNotBeyondContigCoordinates(endPosition, coordinateSystem, contig.length());
        this.startZeroBased = coordinateSystem == CoordinateSystem.ZERO_BASED ? startPosition.pos() : startPosition.pos() - 1;
        // TODO - check that the positions are valid wrt. contig
    }

    private static Position checkNotBeyondContigCoordinates(Position position, CoordinateSystem coordinateSystem, int contigLength) {
        if (position == null) {
            throw new NullPointerException("Position cannot be null");
        }
        // adjust contig start to the coordinate system in use
        int contigStart = 1 - coordinateSystem.delta(CoordinateSystem.ONE_BASED);
        if (position.minPos() + coordinateSystem.delta(CoordinateSystem.ONE_BASED) < contigStart) {
            throw new IllegalArgumentException("Cannot create genomic region with a position " + position + " that extends beyond contig start " + contigStart);
        }
        // no need to adjust contig end since it is one-based for all coordinate systems that we have
        if (position.maxPos() > contigLength) {
            throw new IllegalArgumentException("Cannot create genomic region with a position " + position + " that extends beyond contig end " + contigLength);
        }
        return position;
    }

    public static GenomicRegionDefault of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return new GenomicRegionDefault(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    public static GenomicRegionDefault oneBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return of(contig, strand, CoordinateSystem.ONE_BASED, startPosition, endPosition);
    }

    public static GenomicRegionDefault zeroBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return of(contig, strand, CoordinateSystem.ZERO_BASED, startPosition, endPosition);
    }


    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public GenomicRegionDefault withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        return new GenomicRegionDefault(contig, strand, coordinateSystem, normalisedStartPosition(coordinateSystem), endPosition);
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
    public GenomicRegionDefault withStrand(Strand other) {
        if (strand == other) {
            return this;
        }
        Position start = startPosition.invert(contig, coordinateSystem);
        Position end = endPosition.invert(contig, coordinateSystem);
        return new GenomicRegionDefault(contig, other, coordinateSystem, end, start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenomicRegionDefault)) return false;
        GenomicRegionDefault that = (GenomicRegionDefault) o;
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
