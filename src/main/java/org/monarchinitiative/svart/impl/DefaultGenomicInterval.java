package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicInterval;
import org.monarchinitiative.svart.Strand;

import java.util.Objects;

public record DefaultGenomicInterval(Contig contig, Strand strand, Coordinates coordinates) implements GenomicInterval {

    public DefaultGenomicInterval {
        Objects.requireNonNull(contig, "contig must not be null");
        Objects.requireNonNull(strand, "strand must not be null");
        Objects.requireNonNull(coordinates, "coordinates must not be null");
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
    }

    public static GenomicInterval of(Contig contig, Strand strand, Coordinates coordinates) {
        return new DefaultGenomicInterval(contig, strand, coordinates);
    }

    @Override
    public String toString() {
        return "GenomicInterval{" +
                "contig=" + contigId() +
                ", strand=" + strand() +
                ", " + formatCoordinates() +
                '}';
    }

    private String formatCoordinates() {
        if (coordinates.isPrecise()) {
            return "coordinateSystem=" + coordinateSystem() +
                    ", start=" + start() +
                    ", end=" + end();
        }
        return "coordinateSystem=" + coordinateSystem() +
                ", start=" + start() + ' ' + startConfidenceInterval() +
                ", end=" + end() + ' ' + endConfidenceInterval();
    }

}
