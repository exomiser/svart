package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PartialBreakend implements Breakend {

    private final Contig contig;
    private final String id;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position position;

    public PartialBreakend(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        this.contig = Objects.requireNonNull(contig);
        this.coordinateSystem = coordinateSystem;
        this.position = Objects.requireNonNull(position);
        this.strand = Objects.requireNonNull(strand);
        this.id = Objects.requireNonNull(id);
    }

    public static PartialBreakend oneBased(Contig contig, String id, Strand strand, Position position) {
        return new PartialBreakend(contig, id, strand, CoordinateSystem.ONE_BASED, position);
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
    public Strand strand() {
        return strand;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public PartialBreakend withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        int startDelta = this.coordinateSystem.delta(coordinateSystem);
        return new PartialBreakend(contig, id, strand, coordinateSystem, position.shiftPos(startDelta));
    }

    @Override
    public PartialBreakend withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else {
            Position pos = Position.of(contig.length() - position.pos() + 1,
                    position.confidenceInterval().toOppositeStrand());
            return new PartialBreakend(contig, id, strand, coordinateSystem, pos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialBreakend)) return false;
        PartialBreakend that = (PartialBreakend) o;
        return contig.equals(that.contig) &&
                position.equals(that.position) &&
                strand == that.strand &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, position, strand, id);
    }

    @Override
    public String toString() {
        return "PartialBreakend{" +
                "contig=" + contig.id() +
                ", position=" + position +
                ", strand=" + strand +
                ", id='" + id + '\'' +
                '}';
    }
}
