package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

/**
 * 0- or 1-based region e.g. half-open [a,b) or fully closed [a,b] as indicated by the {@link CoordinateSystem}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public record DefaultGenomicRegion(Contig contig, Strand strand, Coordinates coordinates) implements GenomicRegion, Comparable<GenomicRegion> {

    public DefaultGenomicRegion {
        Objects.requireNonNull(contig, "contig must not be null");
        Objects.requireNonNull(strand, "strand must not be null");
        Objects.requireNonNull(coordinates, "coordinates must not be null");
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
    }

    public static DefaultGenomicRegion of(Contig contig, Strand strand, Coordinates coordinates) {
        return new DefaultGenomicRegion(contig, strand, coordinates);
    }

    @Override
    public int compareTo(GenomicRegion o) {
        return GenomicRegion.compare(this, o);
    }


    /**
     * @param coordinateSystem 
     * @return
     */
    @Override
    public GenomicRegion withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return this;
        }
        return new DefaultGenomicRegion(contig, strand, coordinates.withCoordinateSystem(coordinateSystem));
    }

    /**
     * @param strand
     * @return
     */
    @Override
    public GenomicRegion withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        }
        return new DefaultGenomicRegion(contig, strand, coordinates.invert(contig));
    }

    @Override
    public String toString() {
        return "GenomicRegion{" +
                "contig=" + contigId() +
                ", strand=" + strand() +
                ", " + CoordinatesFormat.formatCoordinates(coordinates()) +
                '}';
    }
}
