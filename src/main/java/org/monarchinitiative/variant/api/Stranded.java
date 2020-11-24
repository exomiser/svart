package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Stranded<T> {

    Strand strand();

    T withStrand(Strand strand);

    default T toOppositeStrand() {
        return withStrand(strand().opposite());
    }
}
