package org.monarchinitiative.variant.api;

import java.util.List;

/**
 * General representation of structural as well as small variants.
 * <p>
 * Implementors must ensure that the following invariants are met:
 * <ul>
 *     <li>at least one adjacency is present</li>
 *     <li>adjacencies are stored in representative order for the variant</li>
 * </ul>
 */
public interface SequenceRearrangement extends Stranded<SequenceRearrangement> {

    /**
     * @return list of adjacencies
     */
    List<Adjacency> getAdjacencies();

    default Breakend getLeftmostBreakend() {
        return getAdjacencies().get(0).getLeft();
    }

    default Breakend getRightmostBreakend() {
        int n = getAdjacencies().size();
        return getAdjacencies().get(n - 1).getRight();
    }

    default Contig getLeftmostContig() {
        return getLeftmostBreakend().getContig();
    }

    default Contig getRightmostContig() {
        return getRightmostBreakend().getContig();
    }

    /**
     * @return strand of the leftmost position of the rearrangement
     */
    default Strand getLeftmostStrand() {
        return getLeftmostBreakend().getStrand();
    }

    /**
     * @return strand of the rightmost position of the rearrangement
     */
    default Strand getRightmostStrand() {
        return getRightmostBreakend().getStrand();
    }

    /**
     * Get leftmost position of the rearrangement. The position is on the strand that you get by {@link #getLeftmostStrand()}.
     *
     * @return coordinate of the leftmost position of the rearrangement
     */
    default Position getLeftmostPosition() {
        return getLeftmostBreakend().getPosition();
    }

    /**
     * Get rightmost position of the rearrangement. The position is on the strand that you get by {@link #getRightmostStrand()}.
     *
     * @return coordinate of the rightmost position of the rearrangement
     */
    default Position getRightmostPosition() {
        return getRightmostBreakend().getPosition();
    }

    int hashCode();

    boolean equals(Object o);
}
