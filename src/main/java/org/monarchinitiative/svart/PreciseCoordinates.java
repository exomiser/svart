package org.monarchinitiative.svart;

import java.util.Objects;

import static org.monarchinitiative.svart.CoordinateSystem.ZERO_BASED;

record PreciseCoordinates(CoordinateSystem coordinateSystem, int start, int end) implements Coordinates, Comparable<Coordinates> {

    static final PreciseCoordinates EMPTY = new PreciseCoordinates(ZERO_BASED, 0, 0);

    PreciseCoordinates {
        Objects.requireNonNull(coordinateSystem);
        Coordinates.validateCoordinates(coordinateSystem, start, end);
    }

    public static PreciseCoordinates of(CoordinateSystem coordinateSystem, int start, int end) {
        return new PreciseCoordinates(coordinateSystem, start, end);
    }

    @Override
    public Coordinates withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (coordinateSystem == this.coordinateSystem) {
            return this;
        }
        return new PreciseCoordinates(coordinateSystem, startWithCoordinateSystem(coordinateSystem), endWithCoordinateSystem(coordinateSystem));
    }

    @Override
    public Coordinates asPrecise() {
        return this;
    }

    @Override
    public Coordinates invert(Contig contig) {
        return new PreciseCoordinates(coordinateSystem, invertEnd(contig), invertStart(contig));
    }

    @Override
    public Coordinates extend(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return new PreciseCoordinates(coordinateSystem(), start() - upstream, end() + downstream);
    }

    @Override
    public int compareTo(Coordinates o) {
        return Coordinates.compare(this, o);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
               "coordinateSystem=" + coordinateSystem +
               ", start=" + start +
               ", end=" + end +
               '}';
    }
}
