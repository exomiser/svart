package org.monarchinitiative.variant.api;

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
        other = (Region<?>) other.withCoordinateSystem(coordinateSystem());
        return start() <= other.start() && other.end() <= end();
    }

    default boolean contains(Position position) {
        return contains(position.pos());
    }

    default boolean contains(int position) {
        return startWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED) <= position && position <= endWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
    }

    default boolean overlapsWith(Region<?> other) {
//        other = (Region<?>) other.withCoordinateSystem(coordinateSystem());
//        return start() <= other.end() && other.start() <= end();

        int thisClosedStart = this.startWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
        int otherClosedEnd = other.endWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
        int otherClosedStart = other.startWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
        int thisCloseEnd = this.endWithCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
        return thisClosedStart <= otherClosedEnd && otherClosedStart <= thisCloseEnd;
    }


    default int length() {
        // the easiest way how to calculate length is to use half-open interval coordinates
        return endWithCoordinateSystem(CoordinateSystem.LEFT_OPEN) - startWithCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    static Comparator<Region<?>> naturalOrder() {
        return GenomicComparators.RegionNaturalOrderComparator.INSTANCE;
    }

    static int compare(Region<?> x, Region<?> y) {
        y = (Region<?>) y.withCoordinateSystem(x.coordinateSystem());
        int result = Position.compare(x.startPosition(), y.startPosition());
        if (result == 0) {
            result = Position.compare(x.endPosition(), y.endPosition());
        }
        return result;
    }
}
