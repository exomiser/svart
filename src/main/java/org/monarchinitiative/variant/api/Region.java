package org.monarchinitiative.variant.api;

import java.util.Comparator;

public interface Region extends CoordinateSystemed<Region> {

    /**
     * @return start coordinate of the region
     */
    Position startPosition();

    /**
     * @return end coordinate of the region
     */
    Position endPosition();

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return startPosition().pos();
    }

    default int normalisedStart(Endpoint endpoint) {
        return start() + coordinateSystem().startDelta(endpoint);
    }

    default Position normalisedStartPosition(Endpoint endpoint) {
        return startPosition().shift(coordinateSystem().startDelta(endpoint));
    }

    /**
     * @return end coordinate of the region
     */
    default int end() {
        return endPosition().pos();
    }

    default int normalisedEnd(Endpoint endpoint) {
        return end() + coordinateSystem().endDelta(endpoint);
    }

    default Position normalisedEndPosition(Endpoint endpoint) {
        return endPosition().shift(coordinateSystem().endDelta(endpoint));
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(Region other) {
        other = other.withCoordinateSystem(coordinateSystem());
        return contains(other.start(), other.end());
    }

    default boolean contains(int start, int end) {
        return start() <= start && end <= end();
    }


    default boolean contains(Position position) {
        return contains(position.pos());
    }

    default boolean contains(int position) {
        return normalisedStart(Endpoint.CLOSED) <= position && position <= normalisedEnd(Endpoint.CLOSED);
    }


    default boolean overlapsWith(Region other) {
        other = other.withCoordinateSystem(coordinateSystem());
        return overlapsWith(other.start(), other.end());
    }

    default boolean overlapsWith(int start, int end) {
        return start() <= end && start <= end();
    }


    default int length() {
        // the easiest way how to calculate length is to use half-open interval coordinates
        return normalisedEnd(Endpoint.CLOSED) - normalisedStart(Endpoint.OPEN);
    }

    static Comparator<Region> naturalOrder() {
        return GenomicComparators.RegionNaturalOrderComparator.INSTANCE;
    }

    static int compare(Region x, Region y) {
        y = y.withCoordinateSystem(x.coordinateSystem());
        int result = Integer.compare(x.start(), y.start());
        if (result == 0) {
            result = Integer.compare(x.end(), y.end());
        }
        return result;
    }
}
