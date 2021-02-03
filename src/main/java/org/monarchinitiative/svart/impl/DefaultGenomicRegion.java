package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

/**
 * 0- or 1-based region e.g. half-open [a,b) or fully closed [a,b] as indicated by the {@link CoordinateSystem}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultGenomicRegion extends BaseGenomicRegion<DefaultGenomicRegion> {

    private DefaultGenomicRegion(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        super(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    public static DefaultGenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return new DefaultGenomicRegion(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    @Override
    protected DefaultGenomicRegion newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return new DefaultGenomicRegion(contig, strand, coordinateSystem, startPosition, endPosition);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "GenomicRegion{" +
                "contig=" + contig().id() +
                ", strand=" + strand() +
                ", coordinateSystem=" + coordinateSystem() +
                ", startPosition=" + startPosition() +
                ", endPosition=" + endPosition() +
                '}';
    }
}
