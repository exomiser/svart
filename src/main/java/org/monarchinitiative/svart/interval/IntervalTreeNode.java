package org.monarchinitiative.svart.interval;

/**
 * Half-open interval for construction of an {@link IntervalTree}.
 * <p>
 * Taken from Jannovar, with minor alterations.
 *
 * @param begin  start point of the interval (inclusive)
 * @param end    end point of the interval (exclusive)
 * @param value  the value stored for the Interval
 * @param maxEnd the maximum of this nodes {@link #end} and both of its children's {@link #end}
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @since 2.0.0
 */
public record IntervalTreeNode<T>(int begin, int end, T value, int maxEnd) implements Comparable<IntervalTreeNode<T>> {

    /**
     * @return start point of the interval (inclusive)
     */
    @Override
    public int begin() {
        return begin;
    }

    /**
     * @return end point of the interval (exclusive)
     */
    @Override
    public int end() {
        return end;
    }

    /**
     * @return the maximum of this nodes {@link #end} and both of it children's {@link #end}
     */
    @Override
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

    public int compareTo(IntervalTreeNode<T> o) {
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
