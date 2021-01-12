package org.monarchinitiative.variant.api;

import java.util.Objects;

class TestRegion implements Region {

    private final Position start, end;
    private final CoordinateSystem coordinateSystem;

    static TestRegion of(CoordinateSystem coordinateSystem, int start, int end) {
        return new TestRegion(coordinateSystem, Position.of(start), Position.of(end));
    }

    private TestRegion(CoordinateSystem coordinateSystem, Position start, Position end) {
        this.start = start;
        this.end = end;
        this.coordinateSystem = coordinateSystem;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public Region withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return new TestRegion(coordinateSystem, normalisedStartPosition(coordinateSystem.startEndpoint()), normalisedEndPosition(coordinateSystem.endEndpoint()));
    }

    @Override
    public Position startPosition() {
        return start;
    }

    @Override
    public Position endPosition() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRegion that = (TestRegion) o;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end) && coordinateSystem == that.coordinateSystem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, coordinateSystem);
    }

    @Override
    public String toString() {
        return "TestRegion{" +
                "start=" + start +
                ", end=" + end +
                ", coordinateSystem=" + coordinateSystem +
                '}';
    }
}
