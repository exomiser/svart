package org.monarchinitiative.svart.coordinates;

import org.monarchinitiative.svart.ConfidenceInterval;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Contig;

import java.util.Objects;

public record ImpreciseCoordinates(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) implements Coordinates, Comparable<Coordinates> {

    public ImpreciseCoordinates {
        Objects.requireNonNull(coordinateSystem);
        Objects.requireNonNull(startCi);
        Objects.requireNonNull(endCi);
        Coordinates.validateCoordinates(coordinateSystem, start, end);
    }

    public static ImpreciseCoordinates of(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
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
