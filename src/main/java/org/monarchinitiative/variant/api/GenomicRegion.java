
package org.monarchinitiative.variant.api;


import java.util.Comparator;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicRegion extends Comparable<GenomicRegion>, Stranded<GenomicRegion> {

    // TODO: do this manually below
    Comparator<GenomicRegion> DEFAULT_COMPARATOR = Comparator.comparing(GenomicRegion::contig)
            .thenComparing(GenomicRegion::startPosition)
            .thenComparing(GenomicRegion::endPosition)
            .thenComparing(GenomicRegion::strand);

    /**
     * @return contig where the region is located
     */
    Contig contig();

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
        return onStrand.start() >= start() && onStrand.end() <= end();
    }

    default boolean contains(GenomicPosition genomicPosition) {
        if (this.contig().id() != genomicPosition.contig().id()) {
            return false;
        }
        // TODO: check other suggestions from PR
        // 1-based query position on this region's strand
        int pos = genomicPosition.withStrand(this.strand()).position().oneBasedPos();
        // 0-based coordinates of this region
        int thisStart = startPosition().zeroBasedPos();
        int thisEnd = endPosition().oneBasedPos();

        return thisStart < pos && pos <= thisEnd;
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return DEFAULT_COMPARATOR.compare(this, other);
    }

    int hashCode();

    boolean equals(Object o);
}
