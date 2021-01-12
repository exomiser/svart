package org.monarchinitiative.variant.api;

/**
 *
 * e.g. http://genome.ucsc.edu/blog/the-ucsc-genome-browser-coordinate-counting-systems/
 * <p>
 * Note: Fully open and right open coordinate systems are possible, but you do not want to go down that rabbit hole.
 * The coordinate systems where <em>end</em> endpoint is <code>OPEN</code> are not compatible with {@link Contig}
 * concept. Also, this API does not intend to solve a general problem like that. We want the API to model coordinate
 * systems used in bioinformatics.
 * <p>
 * Never ever ever ever contemplate using right-open regions.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum CoordinateSystem {
    /**
     * A coordinate system where the first base of a sequence is one.
     * In this co-ordinate system, a region is specified by a <em>closed</em> interval. For example, the region between
     * the 3rd and the 7th bases inclusive is [3,7]. The SAM, VCF, GFF and Wiggle formats use the 1-based coordinate
     * system.
     */
    ONE_BASED(Endpoint.CLOSED, Endpoint.CLOSED),

    /**
     * A coordinate system where the first base of a sequence is zero. In this coordinate system, a region is specified
     * by a <em>left-open right-closed</em> interval.
     * <p>
     * For example, the region between the 3rd and the 7th bases, where the end base is included, is (2,7]. The BAM,
     * BCFv2, BED, and PSL formats use the 0-based coordinate system.
     */
    ZERO_BASED(Endpoint.OPEN, Endpoint.CLOSED),

    RIGHT_OPEN(Endpoint.CLOSED, Endpoint.OPEN),

    FULLY_OPEN(Endpoint.OPEN, Endpoint.OPEN);

    private final Endpoint start;
    private final Endpoint end;

    CoordinateSystem(Endpoint start, Endpoint end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the required delta to shift a start position from the <code>current</code> endpoint to the
     * <code>required</code> one.
     *
     * @return an integer in the range [-1, 0, 1]
     */
    public static int startDelta(Endpoint current, Endpoint required) {
        return current == required ? 0 : current == Endpoint.OPEN ? 1 : -1;
    }

    /**
     * Returns the required delta to shift an end position from the <code>current</code> endpoint to the
     * <code>required</code> one.
     *
     * @return an integer in the range [-1, 0, 1]
     */
    public static int endDelta(Endpoint current, Endpoint required) {
        return current == required ? 0 : current == Endpoint.OPEN ? -1 : 1;
    }

    public boolean isOneBased() {
        return this == ONE_BASED;
    }

    public boolean isZeroBased() {
        return this == ZERO_BASED;
    }

    public Endpoint startEndpoint() {
        return start;
    }

    public Endpoint endEndpoint() {
        return end;
    }

    /**
     * Returns the required number of bases to be added to a start position in order to shift the position from
     * <code>this</code> system to the <code>required</code> endpoint type.
     *
     * @param required endpoint
     * @return an integer in the range [-1, 0, 1]
     */
    public int startDelta(Endpoint required) {
        return startDelta(startEndpoint(), required);
    }

    /**
     * Returns the required number of bases to be added to an end position in order to shift the position from
     * <code>this</code> system to the <code>required</code> endpoint type.
     *
     * @param required endpoint
     * @return an integer in the range [-1, 0, 1]
     */
    public int endDelta(Endpoint required) {
        return endDelta(endEndpoint(), required);
    }
}
