package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 */
public class Position implements Comparable<Position> {

    private final int pos;
    private final CoordinateSystem coordinateSystem;
    private final ConfidenceInterval confidenceInterval;

    private Position(int pos, CoordinateSystem coordinateSystem, ConfidenceInterval confidenceInterval) {
        this.pos = pos;
        this.coordinateSystem = coordinateSystem;
        if (this.coordinateSystem == CoordinateSystem.ZERO_BASED && this.pos < 0) {
            throw new IllegalArgumentException(coordinateSystem + " position `" + pos + "` cannot be negative");
        }
        if (this.coordinateSystem == CoordinateSystem.ONE_BASED && this.pos <= 0) {
            throw new IllegalArgumentException(coordinateSystem + " position `" + pos + "` cannot be non-positive");
        }
        this.confidenceInterval = Objects.requireNonNull(confidenceInterval);
    }

    /**
     * Create precise position using given coordinate system.
     *
     * @param pos              position coordinate
     * @param coordinateSystem coordinate system
     * @return precise position
     */
    public static Position of(int pos, CoordinateSystem coordinateSystem) {
        return of(pos, ConfidenceInterval.precise(), coordinateSystem);
    }

    /**
     * Create 1-based precise position.
     *
     * @param pos position coordinate
     * @return precise position
     */
    public static Position of(int pos) {
        return of(pos, ConfidenceInterval.precise());
    }

    public static Position of(int pos, int ciUpstream, int ciDownstream, CoordinateSystem coordinateSystem) {
        return of(pos, ConfidenceInterval.of(ciUpstream, ciDownstream), coordinateSystem);
    }

    public static Position of(int pos, ConfidenceInterval confidenceInterval) {
        return of(pos, confidenceInterval, CoordinateSystem.ONE_BASED);
    }

    public static Position of(int pos, ConfidenceInterval confidenceInterval, CoordinateSystem coordinateSystem) {
        return new Position(pos, coordinateSystem, confidenceInterval);
    }

    public Position withPos(int pos) {
        if (pos == this.pos) {
            return this;
        }
        return of(pos, this.confidenceInterval, this.getCoordinateSystem());
    }

    /**
     * @return position coordinate
     */
    public int getPos() {
        return pos;
    }

    /**
     * @return position coordinate in {@link CoordinateSystem#ZERO_BASED}
     */
    public Position asZeroBased() {
        return toCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    /**
     * @return position coordinate in {@link CoordinateSystem#ZERO_BASED}
     */
    public int asZeroBasedPos() {
        return asZeroBased().getPos();
    }

    /**
     * @return position coordinate in {@link CoordinateSystem#ONE_BASED}
     */
    public Position asOneBased() {
        return toCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    /**
     * @return position coordinate in {@link CoordinateSystem#ONE_BASED}
     */
    public int asOneBasedPos() {
        return asOneBased().getPos();
    }

    public boolean isZeroBased() {
        return coordinateSystem.isZeroBased();
    }

    public boolean isOneBased() {
        return coordinateSystem.isOneBased();
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    // TODO: perhaps it would be best to simply return the int value here? Regions could end up being created using different
    // based coordinates and there is no guaranteed what CoordinateSystem a pos() is returned in.
    public Position toCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (coordinateSystem == this.coordinateSystem) {
            return this;
        }
        return new Position(this.coordinateSystem == CoordinateSystem.ZERO_BASED ? pos + 1 : pos - 1, coordinateSystem, confidenceInterval);
    }

    /**
     * @return confidence interval associated with the position
     */
    public ConfidenceInterval getConfidenceInterval() {
        return confidenceInterval;
    }

    public int getMinPos() {
        return confidenceInterval.getMinPos(pos);
    }

    public int getMaxPos() {
        return confidenceInterval.getMaxPos(pos);
    }

    /**
     * @return true if this position is precise (CI = [0,0])
     */
    public boolean isPrecise() {
        return confidenceInterval.isPrecise();
    }

    @Override
    public int compareTo(Position o) {
        // todo: order by 0-based position?
        int result = Integer.compare(pos, o.pos);
        if (result == 0) {
            result = Math.negateExact(Integer.compare(confidenceInterval.length(), o.confidenceInterval.length()));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return pos == position.pos &&
                Objects.equals(confidenceInterval, position.confidenceInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, confidenceInterval);
    }

    @Override
    public String toString() {
        return ((confidenceInterval.isPrecise()) ? String.valueOf(pos) : pos +"(" + confidenceInterval + ")");
    }

}
