package org.monarchinitiative.svart.region;

import org.monarchinitiative.svart.GenomicInterval;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.coordinates.CoordinatesFormat;

import java.util.Objects;

/**
 * Package-private default implementation of a {@link GenomicInterval}.
 *
 * @param contig
 * @param strand
 * @param coordinates
 */
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
