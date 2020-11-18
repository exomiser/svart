package org.monarchinitiative.variant.api;

import java.util.Comparator;

/**
 * Represents a {@link Position} on a {@link Contig}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicPosition extends Comparable<GenomicPosition>, Stranded<GenomicPosition> {

    // TODO: do this manually below
    Comparator<GenomicPosition> NATURAL_COMPARATOR = Comparator
            .comparing(GenomicPosition::contigId)
            .thenComparing(GenomicPosition::position)
            .thenComparing(GenomicPosition::strand)
            .thenComparing(GenomicPosition::ci);

    Contig contig();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int contigId() {
        return contig().id();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String contigName() {
        return contig().name();
    }

    Position position();

    default int pos() {
        return position().pos();
    }

    default CoordinateSystem coordinateSystem() {
        return position().coordinateSystem();
    }

    default Strand strand() {
        return Strand.POSITIVE;
    }

    default ConfidenceInterval ci() {
        return position().confidenceInterval();
    }

    default int min() {
        return position().minPos();
    }

    default int max() {
        return position().maxPos();
    }

    @Override
    default int compareTo(GenomicPosition o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }
}
