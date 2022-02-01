package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultGenomicBreakend extends BaseGenomicRegion<DefaultGenomicBreakend> implements GenomicBreakend {

    private final String id;

    private DefaultGenomicBreakend(Contig contig, String id, Strand strand, Coordinates coordinates) {
        super(contig, strand, coordinates);
        this.id = Objects.requireNonNull(id);
        if (length() != 0) {
            throw new IllegalArgumentException("Breakend " + contig.id() + " " + strand + " " + coordinateSystem() + " " + start() + "-" + end() + " cannot have a length > 0");
        }
    }

    public static DefaultGenomicBreakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return new DefaultGenomicBreakend(contig, id, strand, coordinates);
    }

        /**
         * Create partial breakend from coordinates in the given {@link CoordinateSystem}.
         */
    public static DefaultGenomicBreakend of(Contig contig, String id, Strand strand, Coordinates coordinates) {
        return new DefaultGenomicBreakend(contig, id, strand, coordinates);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected DefaultGenomicBreakend newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
        return new DefaultGenomicBreakend(contig, id, strand, coordinates);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultGenomicBreakend that = (DefaultGenomicBreakend) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "Breakend{" +
                "contig=" + contigId() +
                ", id='" + id + '\'' +
                ", strand=" + strand() +
                ", " + formatCoordinates() +
                '}';
    }
}
