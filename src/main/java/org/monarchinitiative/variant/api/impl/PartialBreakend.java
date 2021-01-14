package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class PartialBreakend extends BaseGenomicRegion<PartialBreakend> implements Breakend {

    private final String id;

    private PartialBreakend(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end) {
        super(contig, strand, coordinateSystem, start, end);
        this.id = Objects.requireNonNull(id);
    }

    /**
     * Create partial breakend from a closed position coordinate, such as the start position of {@link CoordinateSystem#FULLY_CLOSED}.
     */
    public static PartialBreakend oneBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.oneBased(), position, position);
    }

    /**
     * Create partial breakend from an open position coordinate, such as a start position in {@link CoordinateSystem#LEFT_OPEN}.
     */
    public static PartialBreakend zeroBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.zeroBased(), position, position);
    }

    /**
     * Create partial breakend from coordinates in the given {@link CoordinateSystem}.
     */
    public static PartialBreakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end) {
        return new PartialBreakend(contig, id, strand, coordinateSystem, start, end);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected PartialBreakend newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end) {
        return new PartialBreakend(contig, id, strand, coordinateSystem, start, end);
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
        return "PartialBreakend{" +
                "contig=" + contig().id() +
                ", id='" + id + '\'' +
                ", strand=" + strand() +
                ", coordinateSystem=" + coordinateSystem() +
                ", startPosition=" + startPosition() +
                ", endPosition=" + endPosition() +
                '}';
    }
}
