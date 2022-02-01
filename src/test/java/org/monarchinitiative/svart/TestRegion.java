package org.monarchinitiative.svart;

import java.util.Objects;

class TestRegion implements Region<TestRegion> {

    private final Coordinates coordinates;

    static TestRegion of(CoordinateSystem coordinateSystem, int start, int end) {
        return new TestRegion(Coordinates.of(coordinateSystem, start, end));
    }

    private TestRegion(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public Coordinates coordinates() {
        return coordinates;
    }

    @Override
    public TestRegion withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return new TestRegion(coordinates.withCoordinateSystem(coordinateSystem));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRegion that = (TestRegion) o;
        return coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates);
    }

    @Override
    public String toString() {
        return "TestRegion{" +
                "coordinateSystem=" + coordinateSystem() +
                ", start=" + start() +
                ", end=" + end() +
                '}';
    }
}
