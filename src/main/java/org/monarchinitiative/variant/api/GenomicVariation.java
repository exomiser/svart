package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicVariation extends GenomicRegion {

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

    default VariantType getVariantType() {
        return VariantType.parseAllele(getRef(), getAlt());
    }

}
