package org.monarchinitiative.svart.interval;

import java.util.List;

/**
 * Type for storing the query result.
 *
 * @param overlaps the values that overlapped with the given point or interval
 * @param left     the value to the left of the given point
 * @param right    the value to the right of the given point
 * @since 2.0.0
 */
public record IntervalOverlaps<T>(List<T> overlaps, T left, T right) {

    private static final IntervalOverlaps<?> EMPTY = new IntervalOverlaps<>(List.of(), null, null);

    public IntervalOverlaps(List<T> overlaps, T left, T right) {
        this.overlaps = List.copyOf(overlaps);
        this.left = left;
        this.right = right;
    }

    public static <T> IntervalOverlaps<T> of(List<T> overlaps) {
        return new IntervalOverlaps<>(overlaps == null ? List.of() : List.copyOf(overlaps), null, null);
    }

    public static <T> IntervalOverlaps<T> neighbours(T left, T right) {
        return new IntervalOverlaps<>(List.of(), left, right);
    }

    @SuppressWarnings("unchecked")
    public static <T> IntervalOverlaps<T> empty() {
        return (IntervalOverlaps<T>) EMPTY;
    }

    public boolean hasOverlaps() {
        return !overlaps.isEmpty();
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    @Override
    public String toString() {
        return "IntervalQueryResult{" +
               "overlapping=" + overlaps.size() +
               ", left=" + left +
               ", right=" + right +
               '}';
    }
}
