
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
     * Returns the length of overlap between this region and another. {@link GenomicRegion}s not overlapping or on
     * another {@link Contig} will return a length of zero. This method will automatically correct for different
     * {@link CoordinateSystem} and {@link Strand}. Note that if the {@link GenomicRegion} are on different strands, the
     * return value will be computed <em>as if they were on the same strand</em>.
     *
     * @param other the other {@link GenomicRegion} to measure the overlap with
     * @return the length of overlap in bases or zero if no overlap.
     */
    default int overlapLength(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            return 0;
        }
        if (this.strand() == other.strand()) {
            return Coordinates.overlapLength(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return Coordinates.overlapLength(coordinateSystem(), start(), end(), other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the start position for this region on the given strand. The resulting position will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} for which the start position is required
     * @return start coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int startOnStrand(Strand strand) {
        return this.strand() == strand ? start() : Coordinates.invertPosition(coordinateSystem(), contig(), end());
    }

    default int startOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem) {
        if (this.strand() == strand && this.coordinateSystem() == coordinateSystem) {
            return start();
        }
        return startOnStrand(strand) + coordinateSystem().startDelta(coordinateSystem);
    }

    /**
     * Returns the end position for this region on the given strand. The resulting position will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} for which the end position is required
     * @return end coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int endOnStrand(Strand strand) {
        return this.strand() == strand ? end() : Coordinates.invertPosition(coordinateSystem(), contig(), start());
    }

    default int endOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem) {
        if (this.strand() == strand && this.coordinateSystem() == coordinateSystem) {
            return end();
        }
        return endOnStrand(strand) + coordinateSystem().endDelta(coordinateSystem);
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
            return Coordinates.distanceAToB(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return Coordinates.distanceAToB(coordinateSystem(), start(), end(), other.coordinateSystem(), otherStart, otherEnd);
    }

    default GenomicRegion withPadding(int padding) {
        return withPadding(padding, padding);
    }

    default GenomicRegion withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return of(contig(), strand(), coordinates().withPadding(upstream, downstream));
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

    @Deprecated
    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, startPosition.pos(), startPosition.confidenceInterval(), endPosition.pos(), endPosition.confidenceInterval());
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
