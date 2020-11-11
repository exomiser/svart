package org.monarchinitiative.variant.api;


import java.util.Comparator;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicRegion extends Comparable<GenomicRegion>, Stranded<GenomicRegion> {

    Comparator<GenomicRegion> DEFAULT_COMPARATOR = Comparator.comparing(GenomicRegion::getContig)
            .thenComparing(GenomicRegion::getStartPosition)
            .thenComparing(GenomicRegion::getEndPosition)
            .thenComparing(GenomicRegion::getStrand);

    /**
     * @return contig where the region is located
     */
    Contig getContig();

    /**
     * @return begin coordinate
     */
    Position getStartPosition();

    /**
     * @return 1-based begin coordinate of the region
     */
    default int getStart() {
        return getStartPosition().asOneBased().getPos();
    }

    /**
     * The begin position is also the end by default
     *
     * @return 1-based end coordinate
     */
    default Position getEndPosition() {
        return getStartPosition();
    }

    /**
     * @return 1-based end coordinate of the region
     */
    default int getEnd() {
        return getEndPosition().asOneBased().getPos();
    }


    default int getLength() {
        return getEnd() - getStart() + 1;
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ZERO_BASED}, where a region
     * is specified by a half-closed-half-open interval
     */
    default boolean isZeroBased() {
        return getStartPosition().isZeroBased() && getEndPosition().isOneBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ONE_BASED}, where a region
     * is specified by a closed interval
     */
    default boolean isOneBased() {
        return getStartPosition().isOneBased() && getEndPosition().isOneBased();
    }

    /**
     * @param other chromosomal region
     * @return true if the region shares at least 1 bp with the <code>other</code> region
     */
    default boolean overlapsWith(GenomicRegion other) {
        if (this.getContig().getId() != other.getContig().getId()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(this.getStrand());
        return getStart() <= onStrand.getEnd() && getEnd() >= onStrand.getStart();
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicRegion other) {
        if (this.getContig().getId() != other.getContig().getId()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(this.getStrand());

        // convert start and end positions to 0-based coordinate system
        int otherStart = onStrand.getStartPosition().asZeroBased().getPos();
        int otherEnd = onStrand.getEndPosition().asOneBased().getPos();
        int thisStart = getStartPosition().asZeroBased().getPos();
        int thisEnd = getEndPosition().asOneBased().asOneBased().getPos();

        return otherStart >= thisStart && otherEnd <= thisEnd;
    }

    default boolean contains(GenomicPosition position) {
        if (this.getContig().getId() != position.getContig().getId()) {
            return false;
        }
        // 1-based query position on this region's strand
        int pos = position.withStrand(getStrand()).getPosition().asOneBased().getPos();
        // 0-based coordinates of this region
        int thisStart = getStartPosition().asZeroBased().getPos();
        int thisEnd = getEndPosition().asOneBased().getPos();

        return thisStart < pos && pos <= thisEnd;
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return DEFAULT_COMPARATOR.compare(this, other);
    }

    int hashCode();

    boolean equals(Object o);
}
