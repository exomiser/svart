package org.monarchinitiative.variant.api;

import java.util.Objects;

class GenomicPositionDefault implements GenomicPosition {

    private final Contig contig;
    private final Position position;
    private final CoordinateSystem coordinateSystem;
    private final Strand strand;

    GenomicPositionDefault(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        if (position.minPos() < 0) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + "that extends beyond first contig base");
        }
        if (position.maxPos() > contig.length()) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + "that extends beyond contig end " + contig.length());
        }

        this.contig = contig;
        this.position = position;
        this.coordinateSystem = coordinateSystem;
        this.strand = strand;
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public GenomicPositionDefault withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        int startDelta = this.coordinateSystem.delta(coordinateSystem);
        return new GenomicPositionDefault(contig, strand, coordinateSystem, position().shiftPos(startDelta));
    }

    @Override
    public GenomicPositionDefault withStrand(Strand strand) {
        if (this.strand.hasComplement()) {
            if (this.strand == strand) {
                return this;
            } else {
                Position pos = coordinateSystem.isOneBased()
                        ? Position.of(contig.length() - pos() + 1, position.confidenceInterval().toOppositeStrand())
                        : Position.of(contig.length() - pos(), position.confidenceInterval().toOppositeStrand());
                return new GenomicPositionDefault(contig, strand, coordinateSystem, pos);
            }
        }
        return this;
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomicPositionDefault that = (GenomicPositionDefault) o;
        return Objects.equals(contig, that.contig) &&
                Objects.equals(position, that.position) &&
                coordinateSystem == that.coordinateSystem &&
                strand == that.strand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, position, coordinateSystem, strand);
    }

    @Override
    public String toString() {
        return coordinateSystem.isOneBased()
                ? '(' + contig.name() + ':' + position + ')' + strand
                : '[' + contig.name() + ':' + position + ')' + strand;
    }
}