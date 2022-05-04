package org.monarchinitiative.svart.interval;

import java.util.Objects;

/**
 * Half-open interval for construction of an {@link IntervalTree}.
 *
 * Taken from Jannovar, with minor alterations.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @since 2.0.0
 */
public class Interval<T> implements Comparable<Interval<T>> {

    /**
     * start point of the interval (inclusive)
     */
    private final int begin;

    /**
     * end point of the interval (exclusive)
     */
    private final int end;

    /**
     * the value stored for the Interval
     */
    private final T value;

    /**
     * the maximum of this nodes {@link #end} and both of its children's {@link #end}
     */
    private final int maxEnd;

    public Interval(int begin, int end, T value, int maxEnd) {
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.maxEnd = maxEnd;
    }

    /**
     * @return start point of the interval (inclusive)
     */
    public int begin() {
        return begin;
    }

    /**
     * @return end point of the interval (exclusive)
     */
    public int end() {
        return end;
    }

    /**
     * @return the value stored for the Interval
     */
    public T value() {
        return value;
    }

    /**
     * @return the maximum of this nodes {@link #end} and both of it children's {@link #end}
     */
    public int maxEnd() {
        return maxEnd;
    }

    /**
     * @return <code>true</code> if <code>point</code> is right of {@link #maxEnd}.
     */
    public boolean allLeftOf(int point) {
        return maxEnd <= point;
    }

    /**
     * @return <code>true</code> if <code>point</code> is right of this interval.
     */
    public boolean isLeftOf(int point) {
        return end <= point;
    }

    /**
     * @return <code>true</code> if <code>point</code> is left of this interval.
     */
    public boolean isRightOf(int point) {
        return point < begin;
    }

    /**
     * @return <code>true</code> if this intervals contains the given point.
     */
    public boolean contains(int point) {
        return begin <= point && point < end;
    }

    /**
     * @return <code>true</code> if this interval overlaps with <code>[begin, end)</code>.
     */
    public boolean overlapsWith(int begin, int end) {
        return begin < this.end && this.begin < end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval<?> interval = (Interval<?>) o;
        return begin == interval.begin && end == interval.end && maxEnd == interval.maxEnd && value.equals(interval.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end, value, maxEnd);
    }

    public int compareTo(Interval<T> o) {
        final int result = begin - o.begin;
        if (result == 0)
            return end - o.end;
        return result;
    }

    @Override
    public String toString() {
        return "Interval [begin=" + begin + ", end=" + end + ", value=" + value + ", maxEnd=" + maxEnd + "]";
    }

}
