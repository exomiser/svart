package org.monarchinitiative.svart;

/**
 * Indicates that a class has a {@link Strand} and can be transposed onto the opposite strand.
 *
 * @param <T>
 */
public interface Transposable<T> extends Stranded {

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
