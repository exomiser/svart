package org.monarchinitiative.variant.api;

/**
 *
 * e.g. http://genome.ucsc.edu/blog/the-ucsc-genome-browser-coordinate-counting-systems/
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum CoordinateSystem {
    /**
     * A coordinate system where the first base of a sequence is one.
     * In this co-ordinate system, a region is specified by a closed interval.  For example, the region between the 3rd
     * and the 7th bases inclusive is [3,7].  The SAM, VCF, GFF and Wiggle formats use the 1-based coordinate system.
     */
    ONE_BASED,

    /**
     * A  coordinate  system  where  the  first  base  of  a  sequence  is  zero.   In  this
     * coordinate system,  a region is specified by a half-closed-half-open interval.  For example,  the region between
     * the 3rd and the 7th bases inclusive is [2,7).  The BAM, BCFv2, BED, and PSL formats use the 0-based coordinate
     * system
     */
    ZERO_BASED;

    public boolean isOneBased() {
        return this == ONE_BASED;
    }

    public boolean isZeroBased() {
        return this == ZERO_BASED;
    }
}
