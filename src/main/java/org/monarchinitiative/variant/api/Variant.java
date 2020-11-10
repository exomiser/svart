package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Variant extends GenomicRegion {

    String getId();

    /**
     * @return String with the reference allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getRef();

    /**
     * @return String with the alternative allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getAlt();

    @Override
    Variant withStrand(Strand strand);

    @Override
    default Variant toOppositeStrand() {
        return withStrand(getStrand().opposite());
    }

    default VariantType getType() {
        return VariantType.parseAllele(getRef(), getAlt());
    }

    boolean isSymbolic();

}
