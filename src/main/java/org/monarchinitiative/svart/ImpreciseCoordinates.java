package org.monarchinitiative.svart;

import java.util.Objects;

record ImpreciseCoordinates(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) implements Coordinates, Comparable<Coordinates> {

    ImpreciseCoordinates {
        Objects.requireNonNull(coordinateSystem);
        Objects.requireNonNull(startCi);
        Objects.requireNonNull(endCi);
        Coordinates.validateCoordinates(coordinateSystem, start, end);
    }

    static ImpreciseCoordinates of(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        return new ImpreciseCoordinates(coordinateSystem, start, startCi, end, endCi);
    }

    @Override
    public ConfidenceInterval startConfidenceInterval() {
        return startCi;
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
        return new PreciseCoordinates(coordinateSystem, start, end);
    }

    @Override
    public Coordinates invert(Contig contig) {
        return new ImpreciseCoordinates(coordinateSystem, invertEnd(contig), endCi.invert(), invertStart(contig), startCi.invert());
    }

    @Override
    public Coordinates extend(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return new ImpreciseCoordinates(coordinateSystem(), start() - upstream, startCi, end() + downstream, endCi);    }


    @Override
    public int compareTo(Coordinates o) {
        return Coordinates.compare(this, o);
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
