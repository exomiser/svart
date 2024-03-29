package org.monarchinitiative.svart;

import org.monarchinitiative.svart.util.Seq;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class BaseGenomicVariant<T extends GenomicVariant> extends BaseGenomicRegion<T> implements GenomicVariant {

    private final String id;
    private final String ref;
    private final String alt;
    // breakend-specific identifiers
    private final String mateId;
    private final String eventId;
    // derived fields
    private final VariantType variantType;
    private final int changeLength;

    protected BaseGenomicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        super(contig, strand, coordinates);
        this.id = IdCache.cacheId(id);
        this.ref = AlleleCache.cacheAllele(validateRefAllele(ref));
        this.alt = AlleleCache.cacheAllele(validateAltAllele(alt));
        this.mateId = Objects.requireNonNullElse(mateId, "");
        this.eventId = Objects.requireNonNullElse(eventId, "");
        this.variantType = VariantType.parseType(ref, alt);
        this.changeLength = checkChangeLength(coordinates, changeLength, variantType);
    }

    protected BaseGenomicVariant(Builder<?> builder) {
        this(builder.contig, builder.id, builder.strand, builder.coordinates, builder.ref, builder.alt, builder.changeLength, builder.mateId, builder.eventId);
    }

    private String validateRefAllele(String ref) {
        return validateAllele(ref, true);
    }

    private String validateAltAllele(String alt) {
        if (alt.length() == 1 && (alt.charAt(0) == '*' || alt.charAt(0) == '.')) {
            // Sonar complains this is a blocker issue:
            // "When a method is designed to return an invariant value, it may be poor design, but it shouldn’t
            // adversely affect the outcome of your program. However, when it happens on all paths through the logic,
            // it is surely a bug.
            // This rule raises an issue when a method contains several return statements that all return the same value."
            //
            // In this case we're running multiple checks and if they pass we want to return the input early, otherwise
            // an error will be thrown by the final test in validateAllele. The return value _should_ always be the input
            // value, in this case!
            return alt;
        }
        return VariantType.isSymbolic(alt) ? alt : validateAllele(alt, false);
    }

    private String validateAllele(String allele, boolean isRef) {
        for (int i = 0; i < allele.length(); i++) {
            if (!validAlleleChar(allele.charAt(i))) {
                throw new IllegalArgumentException("Illegal " + (isRef ? "ref" : "alt") + " allele: " + allele);
            }
        }
        return allele;
    }

    private boolean validAlleleChar(char c) {
        return switch (c) {
            case 'A', 'a', 'T', 't', 'C', 'c', 'G', 'g', 'N', 'n' -> true;
            default -> false;
        };
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
        if (!isSymbolic()) {
            if (ref.length() != coordinates.length()) {
                throw new IllegalArgumentException("Ref allele length of " + ref.length() + " inconsistent with " + coordinates + " (length " + coordinates.length() + ") ref=" + ref + ", alt=" + alt);
            }
            int expectedChangeLength = alt().length() - ref().length();
            if (changeLength != expectedChangeLength) {
                throw new IllegalArgumentException("Given changeLength of " + changeLength + " inconsistent with expected changeLength of " + expectedChangeLength + " for variant " + changeCoordinates());
            }
        }
        return changeLength;
    }

    private String changeCoordinates() {
        return contigId() + ":" + start() + "-" + end() + " " + (ref.isEmpty() ? "-" : ref) + ">" + (alt.isEmpty() ? "-" : alt);
    }

    protected static int calculateChangeLength(String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return alt.length() - ref.length();
    }

    protected static String requireLengthIfSymbolic(String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Missing changeLength for symbolic alt allele " + alt);
        }
        return alt;
    }

    /**
     * Calculates the end position of the reference allele in the coordinate system provided.
     */
    protected static int calculateEnd(int start, CoordinateSystem coordinateSystem, String ref, String alt) {
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

    protected abstract T newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId);

    @Override
    protected T newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
        // no-op Not required as the newVariantInstance returns the same type and this is only required for
        // the BaseGenomicRegion.withCoordinateSystem and withStrand methods which are overridden in this class
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return (T) this;
        }
        return newVariantInstance(contig(), id, strand(), coordinates().withCoordinateSystem(coordinateSystem), ref, alt, changeLength, mateId, eventId);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String mateId() {
        return mateId;
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public int changeLength() {
        return changeLength;
    }

    @Override
    public String ref() {
        return ref;
    }

    @Override
    public String alt() {
        return alt;
    }

    @Override
    public VariantType variantType() {
        return variantType;
    }

    @Override
    public boolean isBreakend() {
        return variantType == VariantType.BND;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withStrand(Strand other) {
        if (strand() == other || isBreakend()) {
            return (T) this;
        }

        String refRevComp = Seq.reverseComplement(ref);
        String altRevComp = isSymbolic() ? alt : Seq.reverseComplement(alt);
        return newVariantInstance(contig(), id, other, coordinates().invert(contig()), refRevComp, altRevComp, changeLength, mateId, eventId);
    }

    /**
     * This method considers any instance to be equal to another if they are the same change on the same {@link Strand}
     * of the same {@link Contig}. Therefore, <b>this implementation will ignore the id</b> such that the variants:
     * <pre>
     *     #CHROM    POS ID  REF ALT
     *     1    20000   .   A   T
     *     1    20000   rs12345678   A   T
     * </pre>
     * will be considered equal. This method will not normalise variants to the same strand before checking equality.
     *
     * @param o the other instance to compare
     * @return true if the two {@link GenomicVariant} instances represent the same change on the same {@link Strand}
     * of the same {@link Contig}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseGenomicVariant<?> that = (BaseGenomicVariant<?>) o;
        return changeLength == that.changeLength && ref.equals(that.ref) && alt.equals(that.alt) && mateId.equals(that.mateId) && eventId.equals(that.eventId) && variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ref, alt, mateId, eventId, variantType, changeLength);
    }

    @Override
    public String toString() {
        return "GenomicVariant{" +
                "contig=" + contigId() +
                ", id='" + id + '\'' +
                ", strand=" + strand() +
                ", " + CoordinatesFormat.formatCoordinates(coordinates()) +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                ", length=" + length() +
                ", changeLength=" + changeLength +
                mateIdStr() +
                eventIdStr() +
                '}';
    }

    private String mateIdStr() {
        return mateId.isEmpty() ? "" : ", mateId=" + mateId;
    }

    private String eventIdStr() {
        return eventId.isEmpty() ? "" : ", eventId=" + eventId;
    }

    public abstract static class Builder<T extends Builder<T>> {

        private static final Coordinates DEFAULT_COORDINATES = Coordinates.of(CoordinateSystem.ONE_BASED, 1, 1);

        protected Contig contig;
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

        public T variant(GenomicVariant genomicVariant) {
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

        public T variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
            return variant(contig, strand, coordinates, ref, requireLengthIfSymbolic(alt), calculateChangeLength(ref, alt));
        }

        public T variant(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
            requireLengthIfSymbolic(alt);
            int end = calculateEnd(start, coordinateSystem, ref, alt);
            Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
            return variant(contig, strand, coordinates, ref, alt, calculateChangeLength(ref, alt));
        }

        public T variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
            this.contig = Objects.requireNonNull(contig, "contig must not be null");
            this.strand = Objects.requireNonNull(strand, "strand must not be null");
            this.coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = changeLength;
            return self();
        }

        public T changeLength(int changeLength) {
            this.changeLength = changeLength;
            return self();
        }

        public T id(String id) {
            this.id = Objects.requireNonNullElse(id, this.id);
            return self();
        }

        public T mateId(String mateId) {
            this.mateId = Objects.requireNonNullElse(mateId, this.mateId);
            return self();
        }

        public T eventId(String eventId) {
            this.eventId = Objects.requireNonNullElse(eventId, this.eventId);
            return self();
        }

        /**
         * Classes extending this Builder are <em>strongly</em> advised to call this method when implementing the
         * build() method in order to add the missing end coordinates and changeLength if objects have been created
         * using the precise contig-position-ref-alt pattern for non-symbolic variants.
         */
        protected T selfWithEndIfMissing() {
            // should pos be null? 1 is a bit arbitrary.
            if (coordinates.end() == 1 && changeLength == 0) {
                coordinates = Coordinates.ofAllele(coordinates.coordinateSystem(), coordinates.start(), ref);
                changeLength = calculateChangeLength(ref, alt);
            }
            return self();
        }

        public T asZeroBased() {
            return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        }

        public T asOneBased() {
            return withCoordinateSystem(CoordinateSystem.ONE_BASED);
        }

        public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
            this.coordinates = coordinates.withCoordinateSystem(coordinateSystem);
            return self();
        }

        public T onPositiveStrand() {
            return withStrand(Strand.POSITIVE);
        }

        public T onNegativeStrand() {
            return withStrand(Strand.NEGATIVE);
        }

        public T withStrand(Strand strand) {
            if (this.strand == strand) {
                return self();
            }
            this.strand = strand;
            coordinates = coordinates.invert(contig);
            ref = Seq.reverseComplement(ref);
            alt = VariantType.isSymbolic(alt) ? alt : Seq.reverseComplement(alt);
            return self();
        }

        protected abstract GenomicVariant build();

        protected abstract T self();
    }
}
