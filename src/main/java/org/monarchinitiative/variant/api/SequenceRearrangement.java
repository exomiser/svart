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

    default Contig getLeftmostContig() {
        return getAdjacencies().get(0).getLeft().getContig();
    }

    default Contig getRightmostContig() {
        return getAdjacencies().get(0).getRight().getContig();
    }

    /**
     * @return strand of the leftmost position of the rearrangement
     */
    default Strand getLeftmostStrand() {
        return getAdjacencies().get(0).getStrand();
    }

    /**
     * @return strand of the rightmost position of the rearrangement
     */
    default Strand getRightmostStrand() {
        int n = getAdjacencies().size();
        return getAdjacencies().get(n - 1).getRight().getStrand();
    }

    /**
     * Get leftmost position of the rearrangement. The position is on the strand that you get by {@link #getLeftmostStrand()}.
     *
     * @return coordinate of the leftmost position of the rearrangement
     */
    default Position getLeftmostPosition() {
        return getAdjacencies().get(0).getLeft().getPosition();
    }
    /**
     * Get rightmost position of the rearrangement. The position is on the strand that you get by {@link #getRightmostStrand()}.
     *
     * @return coordinate of the rightmost position of the rearrangement
     */
    default Position getRightmostPosition() {
        int n = getAdjacencies().size();
        return getAdjacencies().get(n - 1).getRight().getPosition();
    }
}
