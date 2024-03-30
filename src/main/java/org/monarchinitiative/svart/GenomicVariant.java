package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.CompactSequenceVariant;
import org.monarchinitiative.svart.impl.DefaultSequenceVariant;
import org.monarchinitiative.svart.impl.SymbolicVariant;
import org.monarchinitiative.svart.util.Seq;

import java.util.Comparator;
import java.util.Objects;

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
 * of the contig on which the variant is located and also of the changelength. Moreover, they will dynamically choose
 * the most suitable representation for the variables provided which could produce significant memory savings and object
 * allocation. Given this it is <em>highly</em> recommended to use the static factory method on this interface.
 * <p>
 * In general, it is recommended that applications use the HTSJDK to parse the VCF file and then convert the alleles into
 * {@link GenomicVariant} using the {@link org.monarchinitiative.svart.util.VcfConverter} as this will coordinate trimming
 * and creation of {@link GenomicVariant} objects.
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
        if (result == 0) {
            result = CoordinateSystem.compare(x.coordinateSystem(), y.coordinateSystem());
        }
        return result;
    }

    static GenomicVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
        return of(contig, "", strand, coordinates, ref, alt);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        requireLengthIfSymbolic(alt);
        if (ref.length() != coordinates.length()) {
            throw new IllegalArgumentException("Ref allele length of " + ref.length() + " inconsistent with " + coordinates + " (length " + coordinates.length() + ") ref=" + ref + ", alt=" + alt);
        }
        if (CompactSequenceVariant.canBeCompactVariant(ref, alt)) {
            return CompactSequenceVariant.of(contig, id, strand, coordinates.coordinateSystem(), coordinates.start(), ref, alt);
        }
        return DefaultSequenceVariant.of(contig, id, strand, coordinates, ref, alt);
    }

    static GenomicVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return of(contig, "", strand, coordinateSystem, start, ref, alt);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        requireLengthIfSymbolic(alt);
        if (CompactSequenceVariant.canBeCompactVariant(ref, alt)) {
            return CompactSequenceVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
        }
        return DefaultSequenceVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
    }

    static GenomicVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return of(contig, "", strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        if (!VariantType.isSymbolic(alt)) {
            return of(contig, id, strand, coordinates, ref, alt);
        }
        return SymbolicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        return of(contig, "", strand, coordinateSystem, start, end, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        if (!VariantType.isSymbolic(alt)) {
            return of(contig, id, strand, coordinateSystem, start, ref, alt);
        }
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return SymbolicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    static GenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        if (!VariantType.isSymbolic(alt)) {
            return of(contig, id, strand, coordinates, ref, alt);
        }
        return SymbolicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    private static void requireLengthIfSymbolic(String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Missing changeLength for symbolic alt allele " + alt);
        }
    }

    // utility methods
    static String validateRefAllele(String ref) {
        return validateAllele(ref, true);
    }

    static String validateAltAllele(String alt) {
        if (alt.length() == 1 && (alt.charAt(0) == '*' || alt.charAt(0) == '.')) {
            return AlleleCache.cacheAllele(alt);
        }
        return VariantType.isSymbolic(alt) ? alt : validateAllele(alt, false);
    }

    private static String validateAllele(String allele, boolean isRef) {
        for (int i = 0; i < allele.length(); i++) {
            if (!validAlleleChar(allele.charAt(i))) {
                throw new IllegalArgumentException("Illegal " + (isRef ? "ref" : "alt") + " allele: " + allele);
            }
        }
        return AlleleCache.cacheAllele(allele);
    }

    private static boolean validAlleleChar(char c) {
        return switch (c) {
            case 'A', 'a', 'T', 't', 'C', 'c', 'G', 'g', 'N', 'n' -> true;
            default -> false;
        };
    }

    /**
     * Returns a cached empty ("") or missing value (".") instance. Nulls will return an empty value. Other
     * identifiers are returned as input.
     *
     * @param id An identifier string.
     * @return A cached "" or "." instance or the original input value
     */
    static String cacheId(String id) {
        return IdCache.cacheId(id);
    }

    static int calculateChangeLength(String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return alt.length() - ref.length();
    }

    /**
     * Calculates the end position of the reference allele in the coordinate system provided.
     */
    static int calculateEnd(int start, CoordinateSystem coordinateSystem, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        // Given the coordinate system (C) and a reference allele starting at start position (S) with Length (L) the end
        // position (E) is calculated as:
        //  C   S  L  E
        //  FC  1  1  1  (S + L - 1)  ('one-based')
        //  LO  0  1  1  (S + L)      ('zero-based')
        //  RO  1  1  2  (S + L)
        //  FO  0  1  2  (S + L + 1)
        return start + ref.length() + Coordinates.endDelta(coordinateSystem);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {

        private static final Coordinates DEFAULT_COORDINATES = Coordinates.of(CoordinateSystem.ONE_BASED, 1, 1);

        protected Contig contig = Contig.unknown();
        // we're primarily interested in VCF coordinates, so we're defaulting to 1-based coordinates on the + strand
        protected Strand strand = Strand.POSITIVE;
        protected Coordinates coordinates = DEFAULT_COORDINATES;

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters to
        // maintain the correct state when finally built.
        protected String id = "";
        protected String ref = "";
        protected String alt = "";

        protected int changeLength = 0;

        // breakend-specific fields
        protected String mateId = "";
        protected String eventId = "";

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters so as
        // to maintain the correct state when finally built.

        public Builder variant(GenomicVariant genomicVariant) {
            Objects.requireNonNull(genomicVariant, "variant cannot be null");
            return variant(genomicVariant.contig(),
                    genomicVariant.strand(),
                    genomicVariant.coordinates(),
                    genomicVariant.ref(),
                    genomicVariant.alt(),
                    genomicVariant.changeLength())
                    .id(genomicVariant.id())
                    .mateId(genomicVariant.mateId())
                    .eventId(genomicVariant.eventId());
        }

        public Builder variant(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
            requireLengthIfSymbolic(alt);
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            int end = GenomicVariant.calculateEnd(start, coordinateSystem, ref, alt);
            this.coordinates = Coordinates.of(coordinateSystem, start, end);
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = GenomicVariant.calculateChangeLength(ref, alt);
            return this;
        }

        public Builder variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
            requireLengthIfSymbolic(alt);
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            this.coordinates = coordinates;
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = GenomicVariant.calculateChangeLength(ref, alt);
            return this;
        }

        public Builder variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            this.coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = checkChangeLength(coordinates, changeLength, VariantType.parseType(ref, alt));
            return this;
        }

        private int checkChangeLength(Coordinates coordinates, int changeLength, VariantType variantType) {
            if (variantType.baseType() == VariantType.DEL && changeLength >= 0) {
                throw new IllegalArgumentException("Illegal DEL changeLength:" + changeLength + ". Should be < 0 given coordinates " + changeCoordinates());
            }
            if (variantType.baseType() == VariantType.INS && changeLength <= 0) {
                throw new IllegalArgumentException("Illegal INS changeLength:" + changeLength + ". Should be > 0 given coordinates " + changeCoordinates());
            }
            if (variantType.baseType() == VariantType.DUP && changeLength <= 0) {
                throw new IllegalArgumentException("Illegal DUP!changeLength:" + changeLength + ". Should be > 0 given coordinates " + changeCoordinates());
            }
            if (!VariantType.isSymbolic(alt)) {
                if (ref.length() != coordinates.length()) {
                    throw new IllegalArgumentException("Ref allele length of " + ref.length() + " inconsistent with " + coordinates + " (length " + coordinates.length() + ") ref=" + ref + ", alt=" + alt);
                }
                int expectedChangeLength = alt.length() - ref.length();
                if (changeLength != expectedChangeLength) {
                    throw new IllegalArgumentException("Given changeLength of " + changeLength + " inconsistent with expected changeLength of " + expectedChangeLength + " for variant " + changeCoordinates());
                }
            }
            return changeLength;
        }

        private String changeCoordinates() {
            return contig.id() + ":" + coordinates.start() + "-" + coordinates.end() + " " + (ref.isEmpty() ? "-" : ref) + ">" + (alt.isEmpty() ? "-" : alt);
        }

        private static String requireLengthIfSymbolic(String alt) {
            if (VariantType.isSymbolic(alt)) {
                throw new IllegalArgumentException("Missing changeLength for symbolic alt allele " + alt);
            }
            return alt;
        }

        public Builder changeLength(int changeLength) {
            this.changeLength = checkChangeLength(coordinates, changeLength, VariantType.parseType(ref, alt));
            return this;
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNullElse(id, this.id);
            return this;
        }

        public Builder mateId(String mateId) {
            this.mateId = Objects.requireNonNullElse(mateId, this.mateId);
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = Objects.requireNonNullElse(eventId, this.eventId);
            return this;
        }

        public Builder asZeroBased() {
            return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        }

        public Builder asOneBased() {
            return withCoordinateSystem(CoordinateSystem.ONE_BASED);
        }

        public Builder withCoordinateSystem(CoordinateSystem coordinateSystem) {
            this.coordinates = coordinates.withCoordinateSystem(coordinateSystem);
            return this;
        }

        public Builder onPositiveStrand() {
            return withStrand(Strand.POSITIVE);
        }

        public Builder onNegativeStrand() {
            return withStrand(Strand.NEGATIVE);
        }

        public Builder withStrand(Strand strand) {
            if (this.strand == strand) {
                return this;
            }
            this.strand = strand;
            coordinates = coordinates.invert(contig);
            ref = Seq.reverseComplement(ref);
            alt = VariantType.isSymbolic(alt) ? alt : Seq.reverseComplement(alt);
            return this;
        }

        public GenomicVariant build() {
            // should pos be null? 1 is a bit arbitrary.
            if (coordinates.end() == 1 && changeLength == 0) {
                coordinates = Coordinates.ofAllele(coordinates.coordinateSystem(), coordinates.start(), ref);
                changeLength = calculateChangeLength(ref, alt);
            }
            return GenomicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
        }
    }
}
