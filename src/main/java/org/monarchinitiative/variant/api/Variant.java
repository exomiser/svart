package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Variant extends GenomicRegion {

    String id();

    /**
     * @return String with the reference allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String ref();

    /**
     * @return String with the alternative allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String alt();

    /**
     *  Length of the variant on the reference sequence.
     * @return
     */
    default int refLength() {
        return length();
    }

    int changeLength();

    @Override
    Variant withStrand(Strand other);

    @Override
    default Variant toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    default VariantType variantType() {
        return VariantType.parseType(ref(), alt());
    }

    boolean isSymbolic();

}
