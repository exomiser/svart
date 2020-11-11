package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Stranded<T> {

    Strand getStrand();

    T withStrand(Strand strand);

    /**
     * Convert to the opposite strand no matter what.
     */
    default T toOppositeStrand() {
        return withStrand(getStrand().opposite());
    }
}
