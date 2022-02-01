package org.monarchinitiative.svart;

import java.util.Objects;

class ImpreciseCoordinates implements Coordinates {

    private final CoordinateSystem coordinateSystem;
    private final int start;
    private final ConfidenceInterval startCi;
    private final int end;
    private final ConfidenceInterval endCi;

    private ImpreciseCoordinates(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem);
        this.start = start;
        this.startCi = Objects.requireNonNull(startCi);
        this.end = end;
        this.endCi = Objects.requireNonNull(endCi);
        Coordinates.validateCoordinates(this.coordinateSystem, this.start, this.end);
    }

    static ImpreciseCoordinates of(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        return new ImpreciseCoordinates(coordinateSystem, start, startCi, end, endCi);
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public ConfidenceInterval startConfidenceInterval() {
        return startCi;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public ConfidenceInterval endConfidenceInterval() {
        return endCi;
    }

    @Override
    public Coordinates withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (coordinateSystem == this.coordinateSystem) {
            return this;
        }
        return new ImpreciseCoordinates(coordinateSystem, startWithCoordinateSystem(coordinateSystem), startCi, endWithCoordinateSystem(coordinateSystem), endCi);
    }

    @Override
    public Coordinates asPrecise() {
        return new PreciseCoordinates(this.coordinateSystem, this.start, this.end);
    }

    @Override
    public Coordinates invert(Contig contig) {
        return new ImpreciseCoordinates(coordinateSystem, invertEnd(contig), endCi.invert(), invertStart(contig), startCi.invert());
    }

    @Override
    public Coordinates withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return new ImpreciseCoordinates(coordinateSystem(), start() - upstream, startCi, end() + downstream, endCi);    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImpreciseCoordinates that = (ImpreciseCoordinates) o;
        return start == that.start && end == that.end && coordinateSystem == that.coordinateSystem && startCi.equals(that.startCi) && endCi.equals(that.endCi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinateSystem, start, startCi, end, endCi);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "coordinateSystem=" + coordinateSystem +
                ", start=" + start +
                ", startCi=" + startCi +
                ", end=" + end +
                ", endCi=" + endCi +
                '}';
    }
}
