package org.monarchinitiative.svart.interval;

import java.util.Objects;

/**
 * Mutable half-open interval, for incremental building of {@link Interval} objects.
 *
 * Taken from Jannovar, with minor alterations.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 * @since 2.0.0
 */
class MutableInterval<T> implements Comparable<MutableInterval<T>> {

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
    private int maxEnd;


    public MutableInterval(int begin, int end, T value) {
        this.begin = begin;
        this.end = end;
        this.value = value;
        this.maxEnd = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public T getValue() {
        return value;
    }

    public int getMaxEnd() {
        return maxEnd;
    }

    public void setMaxEnd(int maxEnd) {
        this.maxEnd = maxEnd;
    }

    @Override
    public int compareTo(MutableInterval<T> o) {
        final int result = begin - o.begin;
        if (result == 0) {
            return end - o.end;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableInterval<?> that = (MutableInterval<?>) o;
        return begin == that.begin && end == that.end && maxEnd == that.maxEnd && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end, value, maxEnd);
    }

    @Override
    public String toString() {
        return "MutableInterval{" +
                "begin=" + begin +
                ", end=" + end +
                ", value=" + value +
                ", maxEnd=" + maxEnd +
                '}';
    }
}
