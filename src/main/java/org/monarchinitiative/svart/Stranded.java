package org.monarchinitiative.svart;

/**
 * Marker interface to flag a class as having a {@link Strand}. If the class requires the ability to be transposed onto
 * the opposite strand, then it should implement the {@link Transposable} interface.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Stranded {

    Strand strand();

}
