package org.monarchinitiative.svart;

import java.util.Comparator;

/**
 * Compositional interface implementing behaviour for classes with {@link Coordinates}. For example, a region on a
 * sequence nucleic acid or protein sequence.
 */
public interface Interval extends CoordinateSystemed {

    Coordinates coordinates();

    @Override
    default CoordinateSystem coordinateSystem() {
        return coordinates().coordinateSystem();
    }

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return coordinates().start();
    }

    default int startWithCoordinateSystem(CoordinateSystem target) {
        return coordinates().startWithCoordinateSystem(target);
    }

    default ConfidenceInterval startConfidenceInterval() {
        return coordinates().startConfidenceInterval();
    }

    default int startMin() {
        return startConfidenceInterval().minPos(start());
    }

    default int startMax() {
        return startConfidenceInterval().maxPos(start());
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

    default ConfidenceInterval endConfidenceInterval() {
        return coordinates().endConfidenceInterval();
    }

    default int endMin() {
        return endConfidenceInterval().minPos(end());
    }

    default int endMax() {
        return endConfidenceInterval().maxPos(end());
    }

    default int length() {
        return coordinates().length();
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(Interval other) {
        return coordinates().contains(other.coordinates());
    }

    default boolean contains(int position) {
        return startWithCoordinateSystem(CoordinateSystem.ONE_BASED) <= position && position <= endWithCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    default boolean overlapsWith(Interval other) {
        return coordinates().overlaps(other.coordinates());
    }

    default int overlapLength(Interval other) {
        return coordinates().overlapLength(other.coordinates());
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
    default int distanceTo(Interval other) {
        return coordinates().distanceTo(other.coordinates());
    }

    static Comparator<Interval> naturalOrder() {
        return GenomicComparators.IntervalNaturalOrderComparator.INSTANCE;
    }

    static int compare(Interval x, Interval y) {
        return Coordinates.compare(x.coordinates(), y.coordinates());
    }
}