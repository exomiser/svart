package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.BreakendVariant;
import org.monarchinitiative.variant.api.impl.DefaultVariant;

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
        return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    default Variant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
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

    static Variant nonSymbolic(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend alleles " + ref + " " + alt);
        }
        return DefaultVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
    }

    static Variant symbolic(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end, String ref, String alt, int changeLength) {
        if (!VariantType.isLargeSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create symbolic variant from alleles " + ref + " " + alt);
        }
        return DefaultVariant.of(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength);
    }

    static Variant breakend(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return BreakendVariant.of(eventId, left, right, ref, alt);
    }
}
