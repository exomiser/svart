package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class PartialBreakend implements Breakend {

    private final Contig contig;
    private final String id;
    private final Strand strand;
    private final Position position;

    private PartialBreakend(Contig contig, String id, Strand strand, Position position) {
        this.contig = Objects.requireNonNull(contig);
        this.position = Objects.requireNonNull(position);
        this.strand = Objects.requireNonNull(strand);
        this.id = Objects.requireNonNull(id);
    }

    /**
     * Create partial breakend from coordinates in {@link CoordinateSystem#ONE_BASED} system.
     *
     * Note that the returned breakend is always in {@link CoordinateSystem#ZERO_BASED}.
     */
    public static PartialBreakend oneBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.ONE_BASED, position);
    }

    /**
     * Create partial breakend from coordinates in {@link CoordinateSystem#ZERO_BASED} system.
     *
     * Note that the returned breakend is always in {@link CoordinateSystem#ZERO_BASED}.
     */
    public static PartialBreakend zeroBased(Contig contig, String id, Strand strand, Position position) {
        return of(contig, id, strand, CoordinateSystem.ZERO_BASED, position);
    }

    /**
     * Create partial breakend from coordinates in {@link CoordinateSystem#ZERO_BASED} system.
     *
     * Note that the returned breakend is always in {@link CoordinateSystem#ZERO_BASED}.
     */
    public static PartialBreakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return new PartialBreakend(contig, id, strand, position.shift(coordinateSystem.startDelta(CoordinateSystem.ZERO_BASED)));
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
    public PartialBreakend withStrand(Strand other) {
        if (strand == other) {
            return this;
        }
        Position pos = position.invert(contig, CoordinateSystem.ZERO_BASED); // GenomicPosition is always ZERO_BASED
        return new PartialBreakend(contig, id, other, pos);
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
                ", id='" + id +
                ", position=" + position +
                ", strand=" + strand + '\'' +
                '}';
    }
}
