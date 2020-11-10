
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
     * @return 1-based begin coordinate
     */
    Position getStartPosition();

    /**
     * @return 1-based begin coordinate of the region
     */
    default int getStart() {
        return getStartPosition().getPos();
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
        return getEndPosition().getPos();
    }


    default int getLength() {
        return getEnd() - getStart() + 1;
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
        return onStrand.getStart() >= getStart() && onStrand.getEnd() <= getEnd();
    }

    default boolean contains(GenomicPosition position) {
        if (this.getContig().getId() != position.getContig().getId()) {
            return false;
        }
        GenomicPosition onStrand = position.withStrand(this.getStrand());
        return getStart() <= onStrand.getPos() && onStrand.getPos() <= getEnd();
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return DEFAULT_COMPARATOR.compare(this, other);
    }

    int hashCode();

    boolean equals(Object o);
}
