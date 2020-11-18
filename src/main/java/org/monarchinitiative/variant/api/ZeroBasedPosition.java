package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ZeroBasedPosition implements Position {

    private final int pos;
    private final ConfidenceInterval confidenceInterval;

    ZeroBasedPosition(int pos, ConfidenceInterval confidenceInterval) {
        if (pos < 0) {
            throw new IllegalArgumentException("Cannot create zero-based position `" + pos + "` with negative value");
        }
        this.pos = pos;
        this.confidenceInterval = Objects.requireNonNull(confidenceInterval);
    }

    @Override
    public Position withPos(int pos) {
        return pos == this.pos ? this : new ZeroBasedPosition(pos, confidenceInterval);
    }

    /**
     * @return one based position
     */
    @Override
    public int pos() {
        return pos;
    }

    @Override
    public int zeroBasedPos() {
        return pos;
    }

    @Override
    public int oneBasedPos() {
        return pos + 1;
    }

    @Override
    public ConfidenceInterval confidenceInterval() {
        return confidenceInterval;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return CoordinateSystem.ZERO_BASED;
    }

    @Override
    public Position toCoordinateSystem(CoordinateSystem coordinateSystem) {
        return coordinateSystem == CoordinateSystem.ZERO_BASED ? this : new OneBasedPosition(pos + 1, confidenceInterval);
    }


    @Override
    public int hashCode() {
        return Objects.hash(pos, CoordinateSystem.ZERO_BASED, confidenceInterval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZeroBasedPosition)) return false;
        ZeroBasedPosition that = (ZeroBasedPosition) o;
        return pos == that.pos &&
                confidenceInterval.equals(that.confidenceInterval);
    }

    @Override
    public String toString() {
        return "ZeroBasedPosition{" +
                "pos=" + pos +
                ", confidenceInterval=" + confidenceInterval +
                '}';
    }
}
