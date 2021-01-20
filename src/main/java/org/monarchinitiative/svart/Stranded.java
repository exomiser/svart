package org.monarchinitiative.svart;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Stranded<T> {

    Strand strand();

    T withStrand(Strand other);

    default T toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    default T toPositiveStrand() {
        return withStrand(Strand.POSITIVE);
    }

    default T toNegativeStrand() {
        return withStrand(Strand.NEGATIVE);
    }
}
