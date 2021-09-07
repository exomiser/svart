package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultBreakendVariant;
import org.monarchinitiative.svart.impl.DefaultVariant;

import java.util.Comparator;

import static org.monarchinitiative.svart.GenomicComparators.*;

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

    int changeLength();

    @Override
    Variant withStrand(Strand other);

    @Override
    Variant withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    default Variant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    @Override
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

    default boolean isBreakend() {
        return VariantType.isBreakend(alt()) || variantType() == VariantType.BND;
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

    static Variant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return DefaultVariant.of(contig, id, strand, coordinates, ref, alt);
    }

    static Variant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return DefaultVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
    }

    @Deprecated
    static Variant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        int end = BaseVariant.calculateEnd(start.pos(), coordinateSystem, ref, alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, Position.of(end));
        return DefaultVariant.of(contig, id, strand, coordinates, ref, alt);
    }

    static Variant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        VariantType.requireSymbolic(alt);
        return DefaultVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static Variant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        VariantType.requireSymbolic(alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return DefaultVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    @Deprecated
    static Variant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end, String ref, String alt, int changeLength) {
        VariantType.requireSymbolic(alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return DefaultVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static Variant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return DefaultBreakendVariant.of(eventId, left, right, ref, alt);
    }
}
