package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ImprecisePosition implements Position {

    private final int pos;
    private final ConfidenceInterval confidenceInterval;

    private ImprecisePosition(int pos, ConfidenceInterval confidenceInterval) {
        if (pos < 0) {
            throw new IllegalArgumentException("Cannot create position `" + pos + "` with negative value");
        }
        this.pos = pos;
        this.confidenceInterval = Objects.requireNonNull(confidenceInterval);
    }

    static ImprecisePosition of(int pos, ConfidenceInterval confidenceInterval) {
        return new ImprecisePosition(pos, confidenceInterval);
    }

    @Override
    public Position invert(Contig contig, CoordinateSystem coordinateSystem) {
        if (coordinateSystem == CoordinateSystem.ONE_BASED) {
            return Position.of(contig.length() - pos + 1, confidenceInterval().invert());
        }
        return Position.of(contig.length() - pos, confidenceInterval().invert());
    }

    @Override
    public Position withPos(int pos) {
        return pos == this.pos ? this : new ImprecisePosition(pos, confidenceInterval);
    }

    @Override
    public Position shift(int delta) {
        return delta == 0 ? this : new ImprecisePosition(pos + delta, confidenceInterval);
    }

    @Override
    public int pos() {
        return pos;
    }

    @Override
    public ConfidenceInterval confidenceInterval() {
        return confidenceInterval;
    }

    @Override
    public int minPos() {
        return confidenceInterval().minPos(pos());
    }

    @Override
    public int maxPos() {
        return confidenceInterval().maxPos(pos());
    }

    @Override
    public boolean isPrecise() {
        return false;
    }

    @Override
    public Position toPrecise() {
        return PrecisePosition.of(pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, confidenceInterval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImprecisePosition)) return false;
        ImprecisePosition that = (ImprecisePosition) o;
        return pos == that.pos &&
                confidenceInterval.equals(that.confidenceInterval);
    }

    @Override
    public String toString() {
        return pos + " " + confidenceInterval.toString();
    }
}
