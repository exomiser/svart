package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

public record DefaultGenomicInterval(Contig contig, Strand strand, Coordinates coordinates) implements GenomicInterval, Comparable<GenomicInterval> {

    public DefaultGenomicInterval {
        Objects.requireNonNull(contig, "contig must not be null");
        Objects.requireNonNull(strand, "strand must not be null");
        Objects.requireNonNull(coordinates, "coordinates must not be null");
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
    }

    public static DefaultGenomicInterval of(Contig contig, Strand strand, Coordinates coordinates) {
        return new DefaultGenomicInterval(contig, strand, coordinates);
    }

    @Override
    public int compareTo(GenomicInterval o) {
        return GenomicInterval.compare(this, o);
    }

    @Override
    public String toString() {
        return "GenomicInterval{" +
               "contig=" + contigId() +
               ", strand=" + strand() +
               ", " + CoordinatesFormat.formatCoordinates(coordinates) +
               '}';
    }

}
