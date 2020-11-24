package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OneBasedPosition implements Position {

    private final int pos;
    private final ConfidenceInterval confidenceInterval;

    OneBasedPosition(int pos, ConfidenceInterval confidenceInterval) {
        if (pos < 1) {
            throw new IllegalArgumentException("Cannot create one-based position `" + pos + "` with value less than 1");
        }
        this.pos = pos;
        this.confidenceInterval = Objects.requireNonNull(confidenceInterval);
    }

    @Override
    public Position withPos(int pos) {
        return pos == this.pos ? this : new OneBasedPosition(pos, confidenceInterval);
    }

    @Override
    public Position shiftPos(int delta) {
        return delta == 0 ? this : new OneBasedPosition(pos + delta, confidenceInterval);
    }

    @Override
    public int pos() {
        return pos;
    }

    @Override
    public int zeroBasedPos() {
        return pos - 1;
    }

    @Override
    public int oneBasedPos() {
        return pos;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return CoordinateSystem.ONE_BASED;
    }

    @Override
    public Position withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return coordinateSystem == CoordinateSystem.ONE_BASED ? this : new ZeroBasedPosition(pos -1, confidenceInterval);
    }

    @Override
    public ConfidenceInterval confidenceInterval() {
        return confidenceInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OneBasedPosition)) return false;
        OneBasedPosition that = (OneBasedPosition) o;
        return pos == that.pos &&
                confidenceInterval.equals(that.confidenceInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, CoordinateSystem.ONE_BASED, confidenceInterval);
    }

    @Override
    public String toString() {
        return "OneBasedPosition{" +
                "pos=" + pos +
                ", confidenceInterval=" + confidenceInterval +
                '}';
    }
}
