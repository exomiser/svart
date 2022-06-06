package org.monarchinitiative.svart.interval;

/**
 * Normalises the begin and end positions for a type.
 * <p>
 * Taken from Jannovar, with minor alterations.
 *
 * @param <T> the type to normalise
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @since 2.0.0
 */
public interface IntervalNormaliser<T> {

    /**
     * @return start position of <code>x</code> (inclusive)
     */
    public int start(T x);

    /**
     * @return begin position of <code>x</code> (exclusive)
     */
    public int end(T x);

}
