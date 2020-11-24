package org.monarchinitiative.variant.api;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 */
public interface Position extends Comparable<Position>, CoordinateSystemed<Position> {

    /**
     * Create precise position using given coordinate system.
     *
     * @param pos              position coordinate
     * @param coordinateSystem coordinate system
     * @return precise position
     */
    static Position of(int pos, CoordinateSystem coordinateSystem) {
        return of(coordinateSystem, pos, ConfidenceInterval.precise());
    }

    static Position of(int pos, CoordinateSystem coordinateSystem, int ciUpstream, int ciDownstream) {
        return of(coordinateSystem, pos, ConfidenceInterval.of(ciUpstream, ciDownstream));
    }

    /**
     * Create 1-based precise position.
     *
     * @param pos position coordinate
     * @return precise position
     */
    static Position oneBased(int pos) {
        return oneBased(pos, ConfidenceInterval.precise());
    }

    static Position oneBased(int pos, ConfidenceInterval confidenceInterval) {
        return of(CoordinateSystem.ONE_BASED, pos, confidenceInterval);
    }

    static Position zeroBased(int pos) {
        return zeroBased(pos, ConfidenceInterval.precise());
    }

    static Position zeroBased(int pos, ConfidenceInterval confidenceInterval) {
        return of(CoordinateSystem.ZERO_BASED, pos, confidenceInterval);
    }

    /**
     *
     * @param coordinateSystem The coordinate system of the pos argument.
     * @param pos The position in the stated coordinate system
     * @param confidenceInterval confidence interval around the given position
     * @return a {@link Position} in the given coordinateSystem with the specified confidenceInterval
     */
    static Position of(CoordinateSystem coordinateSystem, int pos, ConfidenceInterval confidenceInterval) {
        return coordinateSystem == CoordinateSystem.ONE_BASED ? new OneBasedPosition(pos, confidenceInterval) : new ZeroBasedPosition(pos, confidenceInterval);
    }

    Position withPos(int pos);

    /**
     *
     * @param delta
     * @return a Position shifted by the provided delta
     * */
    Position shiftPos(int delta);

    /**
     * @return one based position
     */
    int pos();

    int zeroBasedPos();

    int oneBasedPos();

    /**
     * @return confidence interval associated with the position
     */
    ConfidenceInterval confidenceInterval();

    default int minPos() {
        return confidenceInterval().minPos(pos());
    }

    default int maxPos() {
        return confidenceInterval().maxPos(pos());
    }

    /**
     * @return true if this position is precise (CI = [0,0])
     */
    default boolean isPrecise() {
        return confidenceInterval().isPrecise();
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param o
     * @return a natural ordering of positions IN ZERO-BASED COORDINATES.
     */
    default int compareTo(Position o) {
        return compare(this, o);
    }

    static int compare(Position x, Position y) {
        int result = Integer.compare(x.zeroBasedPos(), y.zeroBasedPos());
        if (result == 0) {
            result = ConfidenceInterval.compare(x.confidenceInterval(), y.confidenceInterval());
        }
        return result;
    }
}
