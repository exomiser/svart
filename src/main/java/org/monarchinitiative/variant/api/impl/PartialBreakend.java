package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class PartialBreakend extends GenomicPositionDefault implements Breakend {

    private final String id;

    private PartialBreakend(String id,
                            Contig contig,
                            Strand strand,
                            CoordinateSystem coordinateSystem,
                            Position position) {
        super(contig, strand, coordinateSystem, position);
        this.id = Objects.requireNonNull(id, "Id must not be null");
    }

    public static PartialBreakend oneBased(String id, Contig contig, Strand strand, Position position) {
        return of(id, contig, strand, CoordinateSystem.ONE_BASED, position);
    }

    public static PartialBreakend zeroBased(String id, Contig contig, Strand strand, Position position) {
        return of(id, contig, strand, CoordinateSystem.ZERO_BASED, position);
    }

    public static PartialBreakend of(String id, Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return new PartialBreakend(id, contig, strand, coordinateSystem, position);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public PartialBreakend withStrand(Strand strand) {
        if (this.strand.isConvertibleTo(strand)) {
            Position pos = this.strand.opposite() == strand
                    ? coordinateSystem.isOneBased()
                      ? Position.of(contig.length() - pos() + 1, position.confidenceInterval().toOppositeStrand())
                      : Position.of(contig.length() - pos(), position.confidenceInterval().toOppositeStrand())
                    : position;

            return new PartialBreakend(id, contig, strand, coordinateSystem, pos);
        }
        return this;
    }

    @Override
    public PartialBreakend withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        int startDelta = this.coordinateSystem.delta(coordinateSystem);
        return new PartialBreakend(id, contig, strand, coordinateSystem, position().shiftPos(startDelta));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartialBreakend that = (PartialBreakend) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "BND(" + id + ")[" + contig.name() + ":" + position + ']' + strand;
    }
}
