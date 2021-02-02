
package org.monarchinitiative.svart;


import org.monarchinitiative.svart.impl.DefaultGenomicRegion;

import java.util.Comparator;

import static org.monarchinitiative.svart.GenomicComparators.*;

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

    @Override
    GenomicRegion withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @param other chromosomal region
     * @return true if the region shares at least 1 bp with the <code>other</code> region
     */
    default boolean overlapsWith(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            return false;
        }
        if (this.strand() == other.strand()) {
            return Coordinates.overlap(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return Coordinates.overlap(coordinateSystem(), start(), end(), other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the start position for this region on the given strand. The resulting position will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} for which the start position is required
     * @return start coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int startOnStrand(Strand strand) {
        return this.strand() == strand ? start() : Coordinates.invertPosition(coordinateSystem(), end(), contig());
    }

    /**
     * Returns the end position for this region on the given strand. The resulting position will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} for which the end position is required
     * @return end coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int endOnStrand(Strand strand) {
        return this.strand() == strand ? end() : Coordinates.invertPosition(coordinateSystem(), start(), contig());
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicRegion other) {
        if (contig().id() != other.contig().id()) {
            return false;
        }
        if (this.strand() == other.strand()) {
            return Coordinates.aContainsB(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return Coordinates.aContainsB(coordinateSystem(), start(), end(), other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the distance between <code>this</code> and the <code>other</code> regions. The distance represents
     * the number of bases present between the regions.
     * <p>
     * The distance is zero if the <code>a</code> and <code>b</code>
     * are adjacent or if they overlap. The distance is positive if <code>a</code> is downstream of <code>b</code>
     * and negative if <code>a</code> is located downstream from <code>b</code>.
     *
     * @param other genomic region
     * @return distance from <code>this</code> region to the <code>other</code> region
     */
    default int distanceTo(GenomicRegion other) {
        if (contig().id() != other.contig().id()) {
            throw new IllegalArgumentException("Cannot calculate distance between regions on different contigs: " + contig().id() + " <-> " + other.contig().id());
        }
        if (this.strand() == other.strand()) {
            return Coordinates.distanceTo(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return Coordinates.distanceTo(coordinateSystem(), start(), end(), other.coordinateSystem(), otherStart, otherEnd);
    }

    default GenomicRegion withPadding(int padding) {
        return withPadding(padding, padding);
    }

    default GenomicRegion withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return of(contig(), strand(), coordinateSystem(), startPosition().shift(-upstream), endPosition().shift(downstream));
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
