
package org.monarchinitiative.variant.api;


import org.monarchinitiative.variant.api.impl.DefaultGenomicRegion;

import java.util.Comparator;

import static org.monarchinitiative.variant.api.GenomicComparators.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicRegion extends Region<GenomicRegion>, Stranded<GenomicRegion> {

    /**
     * @return contig where the region is located
     */
    Contig contig();

    default int contigId() {
        return contig().id();
    }

    default String contigName() {
        return contig().name();
    }

    /**
     * @param other chromosomal region
     * @return true if the region shares at least 1 bp with the <code>other</code> region
     */
    default boolean overlapsWith(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            return false;
        }
        return overlapsWith((Region<GenomicRegion>) other.withStrand(strand()));
    }

    @Override
    GenomicRegion withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicRegion other) {
        return contig().id() == other.contig().id() && contains((Region<?>) other.withStrand(strand()));
    }

    default GenomicRegion withPadding(int padding) {
        return withPadding(padding, padding);
    }

    default GenomicRegion withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return GenomicRegion.of(contig(), strand(), coordinateSystem(), startPosition().shift(-upstream), endPosition().shift(downstream));
    }

    static Comparator<GenomicRegion> naturalOrder() {
        return GenomicRegionNaturalOrderComparator.INSTANCE;
    }

    static int compare(GenomicRegion x, GenomicRegion y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Region.compare(x, y);
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    int hashCode();

    boolean equals(Object o);

    /**
     * Create genomic position using <em>one-based</em> coordinate system with <em>precise</em> positions on the
     * <em>forward</em> strand.
     *
     * @return one-based position
     */
    static GenomicRegion oneBased(Contig contig, int startPosition, int endPosition) {
        return oneBased(contig, Strand.POSITIVE, Position.of(startPosition), Position.of(endPosition));
    }

    /**
     * Create genomic position using <em>one-based</em> coordinate system with <em>precise</em> positions on the
     * <em>forward</em> strand.
     *
     * @return one-based position
     */
    static GenomicRegion oneBased(Contig contig, Strand strand, int startPosition, int endPosition) {
        return of(contig, strand, CoordinateSystem.FULLY_CLOSED, Position.of(startPosition), Position.of(endPosition));
    }

    /**
     * Create genomic region using <em>one-based</em> coordinate system.
     *
     * @return one-based region
     */
    static GenomicRegion oneBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return of(contig, strand, CoordinateSystem.FULLY_CLOSED, startPosition, endPosition);
    }


    /**
     * Create genomic position using <em>zero-based</em> coordinate system with <em>precise</em> positions on the
     * <em>positive</em> strand.
     *
     * @return zero-based genomic region
     */
    static GenomicRegion zeroBased(Contig contig, int startPosition, int endPosition) {
        return zeroBased(contig, Strand.POSITIVE, startPosition, endPosition);
    }

    /**
     * Create genomic position using <em>one-based</em> coordinate system with <em>precise</em> positions on the
     * <em>forward</em> strand.
     *
     * @return one-based position
     */
    static GenomicRegion zeroBased(Contig contig, Strand strand, int startPosition, int endPosition) {
        return of(contig, strand, CoordinateSystem.LEFT_OPEN, Position.of(startPosition), Position.of(endPosition));
    }

    /**
     * Create genomic region using coordinates in <em>zero-based</em> coordinate system.
     *
     * @return zero-based genomic region
     */
    static GenomicRegion zeroBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return of(contig, strand, CoordinateSystem.LEFT_OPEN, startPosition, endPosition);
    }

    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end) {
        return of(contig, strand, coordinateSystem, Position.of(start), Position.of(end));
    }

    /**
     * Create genomic position on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code>.
     *
     * @return a position
     */
    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return DefaultGenomicRegion.of(contig, strand, coordinateSystem, startPosition, endPosition);
    }
}
