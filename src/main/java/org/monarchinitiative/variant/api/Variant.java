package org.monarchinitiative.variant.api;

import java.util.Comparator;

import static org.monarchinitiative.variant.api.GenomicComparators.*;

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
    Variant withCoordinateSystem(CoordinateSystem coordinateSystem);

    default Variant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    default Variant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    @Override
    default Variant toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    default VariantType variantType() {
        return VariantType.parseType(ref(), alt());
    }

    default boolean isSymbolic() {
        return VariantType.isSymbolic(alt());
    }
    
    static Comparator<? super Variant> naturalOrder() {
        return VariantNaturalOrderComparator.INSTANCE;
    }

    static int compare(Variant x, Variant y) {
        int result = GenomicRegion.compare(x, y);
        if (result == 0) {
            result = x.ref().compareTo(y.ref());
        }
        if (result == 0) {
            result = Integer.compare(x.changeLength(), y.changeLength());
        }
        if (result == 0) {
            result = x.alt().compareTo(y.alt());
        }
        return result;
    }
}
