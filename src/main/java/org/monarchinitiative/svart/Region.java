package org.monarchinitiative.svart;

import java.util.Comparator;

public interface Region<T> extends CoordinateSystemed<T> {

    /**
     * @return start coordinate of the region
     */
    Position startPosition();

    /**
     * @return end coordinate of the region
     */
    Position endPosition();

    @Override
    T withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return startPosition().pos();
    }

    default int startWithCoordinateSystem(CoordinateSystem target) {
        return start() + coordinateSystem().startDelta(target);
    }

    default Position startPositionWithCoordinateSystem(CoordinateSystem target) {
        return startPosition().shift(coordinateSystem().startDelta(target));
    }

    /**
     * @return end coordinate of the region
     */
    default int end() {
        return endPosition().pos();
    }

    default int endWithCoordinateSystem(CoordinateSystem target) {
        return end() + coordinateSystem().endDelta(target);
    }

    default Position endPositionWithCoordinateSystem(CoordinateSystem target) {
        return endPosition().shift(coordinateSystem().endDelta(target));
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(Region<?> other) {
        return start() <= other.startWithCoordinateSystem(coordinateSystem()) && other.endWithCoordinateSystem(coordinateSystem()) <= end();
    }

    default boolean contains(Position position) {
        return contains(position.pos());
    }

    default boolean contains(int position) {
        return startWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED) <= position && position <= endWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
    }

    default boolean overlapsWith(Region<?> other) {
        return Coordinates.overlap(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
    }

    default int length() {
        return Coordinates.length(coordinateSystem(), start(), end());
    }

    static Comparator<Region<?>> naturalOrder() {
        return GenomicComparators.RegionNaturalOrderComparator.INSTANCE;
    }

    static int compare(Region<?> x, Region<?> y) {
        int result = Position.compare(x.startPosition(), y.startPositionWithCoordinateSystem(x.coordinateSystem()));
        if (result == 0) {
            result = Position.compare(x.endPosition(), y.endPositionWithCoordinateSystem(x.coordinateSystem()));
        }
        return result;
    }
}
