package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PrecisePosition implements Position {

    private final int pos;

    private PrecisePosition(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("Cannot create position `" + pos + "` with negative value");
        }
        this.pos = pos;
    }

    public static PrecisePosition of(int pos) {
        return new PrecisePosition(pos);
    }

    @Override
    public Position withPos(int pos) {
        return pos == this.pos ? this : new PrecisePosition(pos);
    }

    @Override
    public Position shiftPos(int delta) {
        return delta == 0 ? this : new PrecisePosition(pos + delta);
    }

    @Override
    public int pos() {
        return pos;
    }

    @Override
    public ConfidenceInterval confidenceInterval() {
        return ConfidenceInterval.precise();
    }

    @Override
    public int minPos() {
        return pos;
    }

    @Override
    public int maxPos() {
        return pos;
    }

    @Override
    public boolean isPrecise() {
        return true;
    }

    @Override
    public Position toPrecise() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrecisePosition)) return false;
        PrecisePosition that = (PrecisePosition) o;
        return pos == that.pos;
    }

    @Override
    public String toString() {
        return Integer.toUnsignedString(pos);
    }
}
