package org.monarchinitiative.svart;

import java.util.Objects;

class PreciseCoordinates implements Coordinates, Comparable<Coordinates> {

    private final CoordinateSystem coordinateSystem;
    private final int start;
    private final int end;

    public PreciseCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem);
        this.start = start;
        this.end = end;
        Coordinates.validateCoordinates(this.coordinateSystem, this.start, this.end);
    }

    public static PreciseCoordinates of(CoordinateSystem coordinateSystem, int start, int end) {
        return new PreciseCoordinates(coordinateSystem, start, end);
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
    public int end() {
        return end;
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
    public Coordinates withPadding(int upstream, int downstream) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreciseCoordinates that = (PreciseCoordinates) o;
        return start == that.start && end == that.end && coordinateSystem == that.coordinateSystem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinateSystem, start, end);
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
