
package org.monarchinitiative.svart;


import org.monarchinitiative.svart.impl.DefaultGenomicRegion;

import java.util.Comparator;

import static org.monarchinitiative.svart.GenomicComparators.*;

/**
 * A {@link GenomicRegion} is a {@link GenomicInterval} e.g. transcribed, regulatory or intergenic regions of a
 * chromosome which is both {@link Transposable} and {@link Convertible}.
 * <p>
 * For regions not requiring the ability to be transposed or converted from one {@link Strand} or {@link CoordinateSystem} to
 * another, the {@link GenomicInterval} interface should be implemented.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicRegion extends GenomicInterval, Convertible<GenomicRegion>, Transposable<GenomicRegion> {

    @Override
    GenomicRegion withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    GenomicRegion withStrand(Strand other);

    default GenomicRegion withPadding(int padding) {
        return withPadding(padding, padding);
    }

    default GenomicRegion withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return of(contig(), strand(), coordinates().extend(upstream, downstream));
    }

    static Comparator<GenomicInterval> naturalOrder() {
        return GenomicIntervalNaturalOrderComparator.INSTANCE;
    }

    static int compare(GenomicRegion x, GenomicRegion y) {
        return GenomicInterval.compare(x, y);
    }

    int hashCode();

    boolean equals(Object o);


    /**
     * Create genomic region on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code> with
     * precise start and end coordinates
     *
     * @return a genomic region
     */
    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end) {
        return of(contig, strand, Coordinates.of(coordinateSystem, start, end));
    }

    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, startCi, end, endCi);
        return DefaultGenomicRegion.of(contig, strand, coordinates);
    }

    /**
     * Create genomic region on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code>.
     *
     * @return a genomic position
     */
    static GenomicRegion of(Contig contig, Strand strand, Coordinates coordinates) {
        return DefaultGenomicRegion.of(contig, strand, coordinates);
    }
}
