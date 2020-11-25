package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 */
public class Position implements Comparable<Position> {

    private final int pos;
    private final ConfidenceInterval confidenceInterval;

    private Position(int pos, ConfidenceInterval confidenceInterval) {
        if (pos < 0) {
            throw new IllegalArgumentException("Cannot create position `" + pos + "` with negative value");
        }
        this.pos = pos;
        this.confidenceInterval = Objects.requireNonNull(confidenceInterval);
    }

    /**
     * Create precise position using given coordinate system.
     *
     * @param pos position coordinate
     * @return precise position
     */
    public static Position of(int pos) {
        return of(pos, ConfidenceInterval.precise());
    }

    public static Position of(int pos, int ciUpstream, int ciDownstream) {
        return of(pos, ConfidenceInterval.of(ciUpstream, ciDownstream));
    }

    /**
     * @param pos                The position in the stated coordinate system
     * @param confidenceInterval confidence interval around the given position
     * @return a {@link Position} in the given coordinateSystem with the specified confidenceInterval
     */
    public static Position of(int pos, ConfidenceInterval confidenceInterval) {
        return new Position(pos, confidenceInterval);
    }

    /**
     * Creates a new {@link Position} using the pos argument with the same {@link ConfidenceInterval} around the point.
     *
     * @param pos numeric position for the new instance
     * @return a new Position instance with the same {@link ConfidenceInterval} as the instance it was derived from
     */
    public Position withPos(int pos) {
        return pos == this.pos ? this : new Position(pos, confidenceInterval);
    }

    /**
     * Creates a new {@link Position} using the delta argument to increment/decrement with pos with the same {@link ConfidenceInterval} around the resulting point.
     *
     * @param delta amount by which to shift the position. Positive inputs will increase the value of pos, negative decrease it.
     * @return a Position shifted by the provided delta
     */
    public Position shiftPos(int delta) {
        return delta == 0 ? this : new Position(pos + delta, confidenceInterval);
    }

    /**
     * @return the numeric position
     */
    public int pos() {
        return pos;
    }

    /**
     * @return confidence interval associated with the position
     */
    public ConfidenceInterval confidenceInterval() {
        return confidenceInterval;
    }

    public int minPos() {
        return confidenceInterval().minPos(pos());
    }

    public int maxPos() {
        return confidenceInterval().maxPos(pos());
    }

    /**
     * @return true if this position is precise (CI = [0,0])
     */
    public boolean isPrecise() {
        return confidenceInterval().isPrecise();
    }

    public Position toPrecise() {
        return new Position(pos, ConfidenceInterval.precise());
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param o
     * @return a natural ordering of positions IN ZERO-BASED COORDINATES.
     */
    @Override
    public int compareTo(Position o) {
        return compare(this, o);
    }

    public static int compare(Position x, Position y) {
        int result = Integer.compare(x.pos(), y.pos());
        if (result == 0) {
            result = ConfidenceInterval.compare(x.confidenceInterval(), y.confidenceInterval());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, CoordinateSystem.ZERO_BASED, confidenceInterval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position that = (Position) o;
        return pos == that.pos &&
                confidenceInterval.equals(that.confidenceInterval);
    }

    @Override
    public String toString() {
        return "Position{" +
                "pos=" + pos +
                ", confidenceInterval=" + confidenceInterval +
                '}';
    }
}
