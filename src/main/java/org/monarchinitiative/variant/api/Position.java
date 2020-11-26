package org.monarchinitiative.variant.api;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 */
public interface Position extends Comparable<Position> {

    /**
     * Create precise position using given coordinate system.
     *
     * @param pos position coordinate
     * @return precise position
     */
    static Position of(int pos) {
        return PrecisePosition.of(pos);
    }

    static Position of(int pos, int ciUpstream, int ciDownstream) {
        return of(pos, ConfidenceInterval.of(ciUpstream, ciDownstream));
    }

    /**
     * @param pos                The position in the stated coordinate system
     * @param confidenceInterval confidence interval around the given position
     * @return a {@link Position} in the given coordinateSystem with the specified confidenceInterval
     */
    static Position of(int pos, ConfidenceInterval confidenceInterval) {
        return confidenceInterval.isPrecise() ? PrecisePosition.of(pos) : ImprecisePosition.of(pos, confidenceInterval);
    }

    /**
     * Creates a new {@link Position} using the pos argument with the same {@link ConfidenceInterval} around the point.
     *
     * @param pos numeric position for the new instance
     * @return a new Position instance with the same {@link ConfidenceInterval} as the instance it was derived from
     */
    Position withPos(int pos);

    /**
     * Creates a new {@link Position} using the delta argument to increment/decrement with pos with the same {@link ConfidenceInterval} around the resulting point.
     *
     * @param delta amount by which to shift the position. Positive inputs will increase the value of pos, negative decrease it.
     * @return a Position shifted by the provided delta
     */
    Position shiftPos(int delta);

    /**
     * @return the numeric position
     */
    int pos();

    /**
     * @return confidence interval associated with the position
     */
    ConfidenceInterval confidenceInterval();

    int minPos();

    int maxPos();

    /**
     * @return true if this position is precise (CI = [0,0])
     */
    boolean isPrecise();

    Position toPrecise();

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param o
     * @return a natural ordering of positions IN ZERO-BASED COORDINATES.
     */
    @Override
    default int compareTo(Position o) {
        return compare(this, o);
    }

    static int compare(Position x, Position y) {
        int result = Integer.compare(x.pos(), y.pos());
        if (result == 0) {
            result = ConfidenceInterval.compare(x.confidenceInterval(), y.confidenceInterval());
        }
        return result;
    }
}
