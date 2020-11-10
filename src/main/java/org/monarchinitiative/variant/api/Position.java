package org.monarchinitiative.variant.api;

import java.util.Comparator;
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
     * @return one based position
     */
    public int getPos() {
        return pos;
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

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
        // todo: order by 0-based position
        return Comparator.comparing(Position::getPos)
                .thenComparing(Position::getConfidenceInterval)
                .compare(this, o);
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
