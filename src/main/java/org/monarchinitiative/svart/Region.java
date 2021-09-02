package org.monarchinitiative.svart;

import java.util.Comparator;

public interface Region<T> extends CoordinateSystemed<T> {

    Coordinates coordinates();

    /**
     * @return start coordinate of the region
     */
    @Deprecated
    default Position startPosition() {
        return Position.of(coordinates().start(), coordinates().startConfidenceInterval());
    }

    /**
     * @return end coordinate of the region
     */
    @Deprecated
    default Position endPosition() {
        return Position.of(coordinates().end(), coordinates().endConfidenceInterval());
    }

    @Override
    default CoordinateSystem coordinateSystem() {
        return coordinates().coordinateSystem();
    }

    @Override
    T withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return coordinates().start();
    }

    default int startWithCoordinateSystem(CoordinateSystem target) {
        return coordinates().startWithCoordinateSystem(target);
    }

    default Position startPositionWithCoordinateSystem(CoordinateSystem target) {
        return Position.of(coordinates().startWithCoordinateSystem(target), coordinates().startConfidenceInterval());
    }

    /**
     * @return end coordinate of the region
     */
    default int end() {
        return coordinates().end();
    }

    default int endWithCoordinateSystem(CoordinateSystem target) {
        return coordinates().endWithCoordinateSystem(target);
    }

    default Position endPositionWithCoordinateSystem(CoordinateSystem target) {
        return Position.of(coordinates().endWithCoordinateSystem(target), coordinates().endConfidenceInterval());
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

    default int overlapLength(Region<?> other) {
        return Coordinates.overlapLength(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
    }

    /**
     * Returns the distance between <code>this</code> and the <code>other</code> regions. The distance represents
     * the number of bases present between the regions.
     * <p>
     * The distance is zero if the <code>a</code> and <code>b</code>
     * are adjacent or if they overlap. The distance is positive if <code>a</code> is downstream of <code>b</code>
     * and negative if <code>a</code> is located downstream from <code>b</code>.
     *
     * @param other region
     * @return distance from <code>this</code> region to the <code>other</code> region
     */
    default int distanceTo(Region<?> other) {
        return Coordinates.distanceAToB(coordinateSystem(), start(), end(), other.coordinateSystem(), other.start(), other.end());
    }

    default int length() {
        return Coordinates.length(coordinateSystem(), start(), end());
    }

    static Comparator<Region<?>> naturalOrder() {
        return GenomicComparators.RegionNaturalOrderComparator.INSTANCE;
    }

    static int compare(Region<?> x, Region<?> y) {
        return Coordinates.compare(x.coordinates(), y.coordinates());
    }
}
