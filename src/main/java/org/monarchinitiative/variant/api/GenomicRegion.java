
package org.monarchinitiative.variant.api;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicRegion extends Comparable<GenomicRegion>, Stranded<GenomicRegion> {

    /**
     * @return contig where the region is located
     */
    Contig contig();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int contigId() {
        return contig().id();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String contigName() {
        return contig().name();
    }

    /**
     * @return 1-based begin coordinate
     */
    Position startPosition();

    /**
     * @return 1-based begin coordinate of the region
     */
    default int start() {
        return startPosition().pos();
    }

    /**
     * The begin position is also the end by default
     *
     * @return 1-based end coordinate
     */
    Position endPosition();

    /**
     * @return 1-based end coordinate of the region
     */
    default int end() {
        return endPosition().pos();
    }


    default int length() {
        return endPosition().oneBasedPos() - startPosition().zeroBasedPos();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ZERO_BASED}, where a region
     * is specified by a half-closed-half-open interval
     */
    default boolean isZeroBased() {
        return startPosition().isZeroBased() && endPosition().isOneBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ONE_BASED}, where a region
     * is specified by a closed interval
     */
    default boolean isOneBased() {
        return startPosition().isOneBased() && endPosition().isOneBased();
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
        GenomicRegion onStrand = other.withStrand(this.strand());
//        return onStrand.start() >= start() && onStrand.end() <= end();

        // convert start and end positions to 0-based coordinate system
        int otherStart = onStrand.startPosition().zeroBasedPos();
        int otherEnd = onStrand.endPosition().oneBasedPos();

        int thisStart = startPosition().zeroBasedPos();
        int thisEnd = endPosition().oneBasedPos();

        return otherStart >= thisStart && otherEnd <= thisEnd;
    }

    default boolean contains(GenomicPosition genomicPosition) {
        if (this.contig().id() != genomicPosition.contig().id()) {
            return false;
        }
        // 1-based query position on this region's strand
        int pos = genomicPosition.withStrand(this.strand()).position().oneBasedPos();
        // 0-based coordinates of this region
        int thisStart = startPosition().zeroBasedPos();
        int thisEnd = endPosition().oneBasedPos();

        return thisStart < pos && pos <= thisEnd;
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return compare(this, other);
    }

    static int compare(GenomicRegion x, GenomicRegion y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Position.compare(x.startPosition(), y.startPosition());
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
