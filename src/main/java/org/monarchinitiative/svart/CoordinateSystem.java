package org.monarchinitiative.svart;

/**
 * Here we model the two coordinate systems used in bioinformatics, the 'one-based' and 'zero-based' systems as recommended
 * by the <a href=https://genomestandards.org/standards/genome-coordinates/>GA4GH GKS workstream</a>. Much has
 * been written about these e.g. <a href=https://genome-blog.gi.ucsc.edu/blog/2016/12/12/the-ucsc-genome-browser-coordinate-counting-systems/>the ucsc genome browser coordinate counting systems</a>
 * (and also the resources linked from there), but for completeness we describe them here.
 * <p>
 * Given we have the sequence ATGC with positions 1-4 using a human friendly 1-start, fully closed system [1,4] (as most
 * people would count using their fingers). A 0-start, fully-closed system would be [0,3], however most systems calculating
 * with intervals use a 'half-open' system where the open end is denoted with a '(' or ')' e.g. [0,3) or (2,4]. The 'open'
 * indicates the coordinate is *not* included so bases TG would have zero-start coordinates [1,3) or (0,2]. Notice that
 * a ONE-start, LEFT-open interval e.g. (1,3] is numerically equivalent to a ZERO-start, RIGHT-open interval e.g [1,3).
 * Zero-based coordinates are also numerically equivalent to 'interbase' coordinates which can be thought of as the
 * coordinates referring to zero-start slices in between the bases.
 * <p>
 * A 0-start, right open is preferable to zero-start, left-open as the first base in a left-open system would be -1, hence
 * zero-start, right-open systems being the preferred system for things like array slices in programming languages e.g.
 * C, C++, Rust, Java, Python where the first element is 0.
 * <p>
 * This is not however a universal truth. Given this, we attempt to bridge the rift between these two camps by providing
 * transparent automatic correction when required, so that a developer need not concern themselves about these other than
 * to be cognisant of the coordinate system of the input coordinates. The library will allow regions of mixed coordinate
 * systems to be safely utilised, although possibly at a small performance cost.
 *
 * <pre>
 * Base sequence                        A T G C
 * One-based  - 1-start, fully closed   1 2 3 4
 *              0-start, fully-closed   0 1 2 3
 * Zero-based - 0-start, right-open    0 1 2 3 4
 *</pre>
 *
 * Zero-based systems are used by BAM, BED, UCSC tools, GA4GH Beacon and VRS
 * One-based systems are used by SAM, VCF, GTF/GFF, UCSC browser, Ensembl, HGVS
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum CoordinateSystem {

    // n.b.
    // ONE_BASED is a 1-start, fully-closed system e.g. [1, 2]
    // ZERO_BASED is a 0-start right-open system e.g. [0, 2)
    // *but* 0-start RIGHT-open is numerically equivalent to a 1-start, LEFT-open system, which is how this is implemented.
    // e.g. 1-start [1, 2] == 1-start (0, 2] == 0-start [0, 2)
    // ONE_BASED start = ZERO_BASED start + 1
    // ZERO_BASED start = ONE_BASED start - 1

    /**
     * A 1-start, fully closed coordinate system.
     * <p>
     * In this coordinate system, a region is specified by a <em>fully-closed</em> interval. For example, in the sequence
     * 'ATGC' the bases 'ATG' will be contained in the region [1,3] in a one-based coordinate system. The SAM, VCF, GFF,
     * Wiggle and HGVS use the 1-based coordinate system.
     * <p>
     * This coordinate system is recommended for use with most humans who find counting from 1 more intuitive.
     */
    ONE_BASED,

    /**
     * 0-start, right-open coordinate system. In this coordinate system, a region is specified by a <em>left-closed
     * right-open</em> interval.
     * <p>
     * For example, in the sequence 'ATGC' the bases 'ATG' will be contained in the region [0,3) in a zero-based
     * coordinate system. The BAM, BCFv2, BED, and PSL formats use the 0-based coordinate system.
     * <p>
     * This coordinate system is recommended for use in computing slices, offsets, overlaps and lengths as there is no
     * need for an additional step to add or subtract 1 when working with a start or end coordinate.
     * <p>
     * Recommended for standard use by the <a href=https://genomestandards.org/standards/genome-coordinates/>GA4GH GKS workstream</a>.
     */
    ZERO_BASED;

    /**
     * A 1-start, fully closed coordinate system.
     * <p>
     * In this coordinate system, a region is specified by a <em>fully-closed</em> interval. For example, in the sequence
     * 'ATGC' the bases 'ATG' will be contained in the region [1,3] in a one-based coordinate system. The SAM, VCF, GFF,
     * Wiggle and HGVS use the 1-based coordinate system.
     * <p>
     * This coordinate system is recommended for use with most humans who find counting from 1 more intuitive.
     * */
    public static CoordinateSystem oneBased() {
        return ONE_BASED;
    }

    /**
     * 0-start, right-open coordinate system. In this coordinate system, a region is specified by a <em>left-closed
     * right-open</em> interval.
     * <p>
     * For example, in the sequence 'ATGC' the bases 'ATG' will be contained in the region [0,3) in a zero-based
     * coordinate system. The BAM, BCFv2, BED, and PSL formats use the 0-based coordinate system.
     * <p>
     * This coordinate system is recommended for use in computing slices, offsets, overlaps and lengths as there is no
     * need for an additional step to add or subtract 1 when working with a start or end coordinate.
     * <p>
     * Recommended for standard use by the <a href=https://genomestandards.org/standards/genome-coordinates/>GA4GH GKS workstream</a>.
     */
    public static CoordinateSystem zeroBased() {
        return ZERO_BASED;
    }

    public boolean isOneBased() {
        return this == ONE_BASED;
    }

    public boolean isZeroBased() {
        return this == ZERO_BASED;
    }

    /**
     * Returns the required number of bases to be added to a start position in order to shift the position from
     * <code>this</code> system to the <code>target</code> system.
     *
     * @param target system
     * @return an integer in the range [-1, 0, 1]
     */
    public int startDelta(CoordinateSystem target) {
        if (this == target) {
            return 0;
        }
        return this == ZERO_BASED ? 1 : -1;
    }
}
