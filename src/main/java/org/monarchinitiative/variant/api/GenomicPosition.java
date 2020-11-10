package org.monarchinitiative.variant.api;

import java.util.Comparator;

/**
 * Represents a {@link Position} on a {@link Contig}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicPosition extends Comparable<GenomicPosition>, Stranded<GenomicPosition> {

    Comparator<GenomicPosition> NATURAL_COMPARATOR = Comparator
            .comparing(GenomicPosition::getContigId)
            .thenComparing(GenomicPosition::getPosition)
            .thenComparing(GenomicPosition::getStrand)
            .thenComparing(GenomicPosition::getCi);

    Contig getContig();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int getContigId() {
        return getContig().getId();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String getContigName() {
        return getContig().getName();
    }

    Position getPosition();

    default int getPos() {
        return getPosition().getPos();
    }

    default CoordinateSystem getCoordinateSystem() {
        return getPosition().getCoordinateSystem();
    }

    default Strand getStrand() {
        return Strand.POSITIVE;
    }

    default ConfidenceInterval getCi() {
        return getPosition().getConfidenceInterval();
    }

    default int getMin() {
        return getPosition().getMinPos();
    }

    default int getMax() {
        return getPosition().getMaxPos();
    }

    @Override
    default int compareTo(GenomicPosition o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }
}
