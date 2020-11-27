package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.SequenceRearrangementDefault;

import java.util.Arrays;
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
    List<Adjacency> adjacencies();

    default Contig leftmostContig() {
        return adjacencies().get(0).left().contig();
    }

    default Contig rightmostContig() {
        return adjacencies().get(0).right().contig();
    }

    /**
     * @return strand of the leftmost position of the rearrangement
     */
    default Strand leftmostStrand() {
        return adjacencies().get(0).strand();
    }

    /**
     * @return strand of the rightmost position of the rearrangement
     */
    default Strand rightmostStrand() {
        int n = adjacencies().size();
        return adjacencies().get(n - 1).right().strand();
    }

    /**
     * Get leftmost position of the rearrangement. The position is on the strand that you get by {@link #leftmostStrand()}.
     *
     * @return coordinate of the leftmost position of the rearrangement
     */
    default Position leftmostPosition() {
        return adjacencies().get(0).left().position();
    }
    /**
     * Get rightmost position of the rearrangement. The position is on the strand that you get by {@link #rightmostStrand()}.
     *
     * @return coordinate of the rightmost position of the rearrangement
     */
    default Position rightmostPosition() {
        int n = adjacencies().size();
        return adjacencies().get(n - 1).right().position();
    }

    static SequenceRearrangement of(Adjacency... adjacencies) {
        return SequenceRearrangementDefault.of(Arrays.asList(adjacencies));
    }

    static SequenceRearrangement of(List<Adjacency> adjacencies) {
        return SequenceRearrangementDefault.of(adjacencies);
    }
}
