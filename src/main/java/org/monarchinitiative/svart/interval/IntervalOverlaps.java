package org.monarchinitiative.svart.interval;

import java.util.List;
import java.util.Objects;

/**
 * Type for storing the query result.
 * @since 2.0.0
 */
public class IntervalOverlaps<T> {

    private static final IntervalOverlaps<?> EMPTY = new IntervalOverlaps<>(List.of(), null, null);

    /**
     * the values that overlapped with the given point or interval
     */
    private final List<T> overlaps;
    /**
     * the value to the left of the given point
     */
    private final T left;
    /**
     * the value to the right of the given point
     */
    private final T right;

    private IntervalOverlaps(List<T> overlaps, T left, T right) {
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

    public List<T> overlaps() {
        return overlaps;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public T left() {
        return left;
    }

    public boolean hasRight() {
        return right != null;
    }

    public T right() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalOverlaps<?> that = (IntervalOverlaps<?>) o;
        return overlaps.equals(that.overlaps) && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overlaps, left, right);
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
