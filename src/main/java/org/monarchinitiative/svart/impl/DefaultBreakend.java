package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultBreakend extends BaseGenomicRegion<DefaultBreakend> implements Breakend {

    private final String id;

    private DefaultBreakend(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        super(contig, strand, coordinateSystem, position, position);
        this.id = Objects.requireNonNull(id);
    }

    /**
     * Create partial breakend from a closed position coordinate, such as the start position of {@link CoordinateSystem#FULLY_CLOSED}.
     */
    public static DefaultBreakend oneBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.oneBased(), position);
    }

    /**
     * Create partial breakend from an open position coordinate, such as a start position in {@link CoordinateSystem#LEFT_OPEN}.
     */
    public static DefaultBreakend zeroBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.zeroBased(), position);
    }

    /**
     * Create partial breakend from coordinates in the given {@link CoordinateSystem}.
     */
    public static DefaultBreakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return new DefaultBreakend(contig, id, strand, coordinateSystem, position);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected DefaultBreakend newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end) {
        return new DefaultBreakend(contig, id, strand, coordinateSystem, start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultBreakend that = (DefaultBreakend) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "Breakend{" +
                "contig=" + contig().id() +
                ", id='" + id + '\'' +
                ", strand=" + strand() +
                ", coordinateSystem=" + coordinateSystem() +
                ", startPosition=" + startPosition() +
                ", endPosition=" + endPosition() +
                '}';
    }
}
