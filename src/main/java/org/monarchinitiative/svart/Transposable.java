package org.monarchinitiative.svart;

interface Transposable<T> extends Stranded {

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
