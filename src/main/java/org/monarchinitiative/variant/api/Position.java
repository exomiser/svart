package org.monarchinitiative.variant.api;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 */
public interface Position extends Comparable<Position> {

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

    /**
     * Create 1-based precise position.
     *
     * @param pos position coordinate
     * @return precise position
     */
    // TODO: rename Position.oneBased() and provide Position.zeroBased() static constructor
    static Position of(int pos) {
        return of(pos, ConfidenceInterval.precise());
    }

    static Position of(int pos, int ciUpstream, int ciDownstream, CoordinateSystem coordinateSystem) {
        return of(coordinateSystem, pos, ConfidenceInterval.of(ciUpstream, ciDownstream));
    }

    static Position of(int pos, ConfidenceInterval confidenceInterval) {
        return of(CoordinateSystem.ONE_BASED, pos, confidenceInterval);
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
     * @return one based position
     */
    int pos();

    int zeroBasedPos();

    int oneBasedPos();

    CoordinateSystem coordinateSystem();

    Position toCoordinateSystem(CoordinateSystem coordinateSystem);

    default boolean isOneBased() {
        return coordinateSystem() == CoordinateSystem.ONE_BASED;
    }

    default boolean isZeroBased() {
        return coordinateSystem() == CoordinateSystem.ZERO_BASED;
    }

    default Position oneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    default Position zeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    default Position withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return coordinateSystem() == coordinateSystem ? this : toCoordinateSystem(coordinateSystem);
    }

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
