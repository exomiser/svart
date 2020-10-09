
package org.monarchinitiative.variant.api;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicRegion extends Comparable<GenomicRegion> {

    public GenomicPosition getStart();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int getStartContigId() {
        return getStart().getContig().getId();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String getStartContigName() {
        return getStart().getContig().getName();
    }

    default int getStartPosition() {
        return getStart().getPosition();
    }

    default ConfidenceInterval getStartCi() {
        return getStart().getCi();
    }

    default int getStartMin() {
        return getStartCi().getMinPos(getStartPosition());
    }

    default int getStartMax() {
        return getStartCi().getMaxPos(getStartPosition());
    }

    public GenomicPosition getEnd();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int getEndContigId() {
        return getEnd().getContig().getId();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String getEndContigName() {
        return getEnd().getContig().getName();
    }

    default int getEndPosition() {
        return getEnd().getPosition();
    }

    default ConfidenceInterval getEndCi() {
        return getEnd().getCi();
    }

    default int getEndMin() {
        return getEndCi().getMinPos(getEndPosition());
    }

    default int getEndMax() {
        return getEndCi().getMaxPos(getEndPosition());
    }

    default int getLength() {
        return getEnd().getPosition() - getStart().getPosition();
    }

}
