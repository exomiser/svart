package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultGenomicBreakendVariant;
import org.monarchinitiative.svart.impl.DefaultGenomicVariant;

import java.util.Comparator;

import static org.monarchinitiative.svart.GenomicComparators.*;

/**
 * A {@link GenomicVariant} represents a variation of the sequence of a {@link GenomicRegion}. It is the key abstraction
 * for representing genomic variation. The {@link GenomicVariant} mostly follows VCF naming conventions for easy interoperability
 * between the VCF file format and a program attempting to use these for an analysis. The {@link GenomicVariant} provides
 * methods to switch the strand or coordinate system of the instance as well as more usual methods for describing the
 * length and type of the variant.
 * <p>
 * For example the following VCF record can be converted into a {@link GenomicVariant} like so:
 * <pre>
 * #CHROM  POS       ID        REF   ALT    QUAL  FILTER  INFO
 * 5       16508565  rs332811  T     C      200   PASS
 *
 * Contig chr5 = GenomicAssemblies.GRCh38p13().contigByName("5");
 * Coordinates pos = Coordinates.oneBased(16508565, 16508565);
 * GenomicVariant variant = GenomicVariant.of(chr5, "rs332811", Strand.POSITIVE, pos, "T", "C");
 * </pre>
 *
 * This class provides overloaded static factory methods ({@linkplain GenomicVariant#of}) which allow for easy creation of instances
 * using default implementations. These implementations provide validation of the input coordinates within the bounds
 * of the contig on which the variant is located and also of the changelength.
 * <p>
 * In general, it is recommended that applications use the HTSJDK to parse the VCF file and then convert the alleles into
 * {@link GenomicVariant} using the {@link org.monarchinitiative.svart.util.VcfConverter} as this will coordinate trimming
 * and creation and allows external libraries to hook into some of the methods by means of providing a {@link org.monarchinitiative.svart.BaseGenomicVariant.Builder}
 * to build custom objects implementing the {@link GenomicVariant} interface.
 * <p>
 * The {@link GenomicVariant} can be used to represent sequence, symbolic or breakend variant types, although the breakend
 * type has a unique ALT allele format used to describe the mate of the current break. These can be represented more
 * fully by the {@link GenomicBreakendVariant} and its companion {@link GenomicBreakend} interface. Due to the differences
 * in their representation it is recommended that applications use the symbolic {@link GenomicVariant} representation,
 * unless they specifically need a {@link GenomicBreakendVariant}.
 * <p>
 * A {@link GenomicVariant} can be converted into a {@link GenomicBreakendVariant} using the
 * {@link org.monarchinitiative.svart.util.VcfBreakendResolver} utility class and the {@link GenomicBreakendVariant} will
 * provide the means of representing itself as a symbolic {@link GenomicVariant} via the
 * {@link GenomicBreakendVariant#toSymbolicGenomicVariant()} method or this can be done manually using the
 * {@link org.monarchinitiative.svart.util.VcfBreakendFormatter} utility.
 *
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
