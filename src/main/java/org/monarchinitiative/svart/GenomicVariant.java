package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultGenomicBreakendVariant;
import org.monarchinitiative.svart.impl.DefaultGenomicVariant;

import java.util.Comparator;

import static org.monarchinitiative.svart.GenomicComparators.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicVariant extends GenomicRegion {

    String id();

    /**
     * For breakend variants. Returns the mateId of the mated breakend if set.
     * @return The mateId or empty if not set.
     */
    default String mateId() {
        return "";
    }

    /**
     * For breakend variants. Returns the eventId of breakend if set.
     * @return The eventId or empty if not set.
     */
    default String eventId() {
        return "";
    }

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
    GenomicVariant withStrand(Strand other);

    @Override
    GenomicVariant withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    default GenomicVariant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    default GenomicVariant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    @Override
    default GenomicVariant toOppositeStrand() {
        return this.isBreakend() ? this : withStrand(strand().opposite());
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

    static Comparator<GenomicVariant> naturalOrder() {
        return GenomicVariantNaturalOrderComparator.INSTANCE;
    }

    static int compare(GenomicVariant x, GenomicVariant y) {
        int result = GenomicInterval.compare(x, y);
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

    static GenomicVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
        return of(contig, "", strand, coordinates, ref, alt);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        return DefaultGenomicVariant.of(contig, id, strand, coordinates, ref, alt);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return DefaultGenomicVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
    }

    static GenomicVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return DefaultGenomicVariant.of(contig, "", strand, coordinateSystem, start, ref, alt);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return DefaultGenomicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return DefaultGenomicVariant.of(contig, "", strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return DefaultGenomicVariant.of(contig, "", strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return DefaultGenomicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        return DefaultGenomicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    static GenomicVariant of(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        return DefaultGenomicBreakendVariant.of(eventId, left, right, ref, alt);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends BaseGenomicVariant.Builder<Builder> {

        @Override
        public GenomicVariant build() {
            return new DefaultGenomicVariant(selfWithEndIfMissing());
        }

        @Override
        protected GenomicVariant.Builder self() {
            return this;
        }
    }
}
