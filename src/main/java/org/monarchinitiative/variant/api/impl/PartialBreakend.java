package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class PartialBreakend extends BaseGenomicPosition<PartialBreakend> implements Breakend {

    private final String id;

    private PartialBreakend(Contig contig, String id, Strand strand, Position position) {
        super(contig, strand, position);
        this.id = Objects.requireNonNull(id);
    }

    /**
     * Create partial breakend from a closed position coordinate, such as the start position of {@link CoordinateSystem#ONE_BASED}.
     */
    public static PartialBreakend oneBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, position);
    }

    /**
     * Create partial breakend from an open position coordinate, such as a start position in {@link CoordinateSystem#ZERO_BASED}.
     */
    public static PartialBreakend zeroBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, position.shift(1));
    }

    /**
     * Create partial breakend from coordinates in {@link CoordinateSystem#ZERO_BASED} system.
     *
     * Note that the returned breakend is always in {@link CoordinateSystem#ZERO_BASED}.
     */
    public static PartialBreakend of(Contig contig, String id, Strand strand, Position position) {
        return new PartialBreakend(contig, id, strand, position);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected PartialBreakend newPositionInstance(Contig contig, Strand strand, Position position) {
        return new PartialBreakend(contig, id, strand, position);
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
                "id='" + id + '\'' +
                "} " + super.toString();
    }
}
