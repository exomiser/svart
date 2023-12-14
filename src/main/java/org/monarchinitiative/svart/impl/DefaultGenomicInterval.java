package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

public record DefaultGenomicInterval(Contig contig, Strand strand, Coordinates coordinates) implements GenomicInterval {

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
    public String toString() {
        return "GenomicInterval{" +
               "contig=" + contigId() +
               ", strand=" + strand() +
               ", " + formatCoordinates(coordinates) +
               '}';
    }

    private String formatCoordinates(Coordinates coordinates) {
        if (coordinates.isPrecise()) {
            return "coordinateSystem=" + coordinates.coordinateSystem() +
                   ", start=" + coordinates.start() +
                   ", end=" + coordinates.end();
        }
        return "coordinateSystem=" + coordinates.coordinateSystem() +
               ", start=" + coordinates.start() + (coordinates.startConfidenceInterval().isPrecise() ? "" : " " + coordinates.startConfidenceInterval()) +
               ", end=" + coordinates.end() + (coordinates.endConfidenceInterval().isPrecise() ? "" : " " + coordinates.endConfidenceInterval());
    }
}
