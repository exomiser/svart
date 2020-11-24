
package org.monarchinitiative.variant.api;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicRegion extends Comparable<GenomicRegion>, Stranded<GenomicRegion> , CoordinateSystemed<GenomicRegion> {

    /**
     * @return contig where the region is located
     */
    Contig contig();

    /**
     * @return start coordinate
     */
    Position startPosition();

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return startPosition().pos();
    }

    /**
     * Returns to zero based start position of the region.
     * @return
     */
    default int startZeroBased() {
        return coordinateSystem().isZeroBased() ? start() : start() - 1;
    }

    /**
     * The begin position is also the end by default
     *
     * @return end coordinate
     */
    Position endPosition();

    /**
     * @return 1-based end coordinate of the region
     */
    default int end() {
        return endPosition().pos();
    }


    default int length() {
        return end() - startZeroBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ZERO_BASED}, where a region
     * is specified by a half-closed-half-open interval
     */
    @Override
    default boolean isZeroBased() {
        return coordinateSystem().isZeroBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ONE_BASED}, where a region
     * is specified by a closed interval
     */
    @Override
    default boolean isOneBased() {
        return coordinateSystem().isOneBased();
    }

    /**
     * @param other chromosomal region
     * @return true if the region shares at least 1 bp with the <code>other</code> region
     */
    default boolean overlapsWith(GenomicRegion other) {
        if (this.contig().id() != other.contig().id()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(this.strand());
        return start() <= onStrand.end() && end() >= onStrand.start();
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicRegion other) {
        if (this.contig().id() != other.contig().id()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(this.strand()).withCoordinateSystem(this.coordinateSystem());
        return onStrand.start() >= start() && onStrand.end() <= end();
    }

    default boolean contains(GenomicPosition genomicPosition) {
        if (this.contig().id() != genomicPosition.contig().id()) {
            return false;
        }
        GenomicPosition onStrand = genomicPosition.withStrand(this.strand()).withCoordinateSystem(this.coordinateSystem());
        return start() >= onStrand.pos() && onStrand.pos() <= end();
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return compare(this, other);
    }

    static int compare(GenomicRegion x, GenomicRegion y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            // calculate normalization delta for start positions
            int delta = x.coordinateSystem().delta(y.coordinateSystem());
            result = Position.compare(x.startPosition(), y.startPosition().shiftPos(delta));
        }
        if (result == 0) {
            result = Position.compare(x.endPosition(), y.endPosition());
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    int hashCode();

    boolean equals(Object o);
}
