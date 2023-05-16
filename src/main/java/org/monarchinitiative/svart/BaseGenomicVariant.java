package org.monarchinitiative.svart;

import org.monarchinitiative.svart.util.Seq;

import java.nio.charset.StandardCharsets;
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
        this.ref = AlleleCache.cacheAllele(VariantType.requireNonSymbolic(ref));
        this.alt = AlleleCache.cacheAllele(alt);
        this.mateId = Objects.requireNonNullElse(mateId, "");
        this.eventId = Objects.requireNonNullElse(eventId, "");
        this.variantType = VariantType.parseType(ref, alt);
        this.changeLength = checkChangeLength(coordinates, changeLength, variantType);
    }

    protected BaseGenomicVariant(Builder<?> builder) {
        this(builder.contig, builder.id, builder.strand, builder.coordinates, builder.ref, builder.alt, builder.changeLength, builder.mateId, builder.eventId);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseGenomicVariant<?> that = (BaseGenomicVariant<?>) o;
        return changeLength == that.changeLength && id.equals(that.id) && ref.equals(that.ref) && alt.equals(that.alt) && mateId.equals(that.mateId) && eventId.equals(that.eventId) && variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, ref, alt, mateId, eventId, variantType, changeLength);
    }

    @Override
    public String toString() {
        return "GenomicVariant{" +
               "contig=" + contigId() +
               ", id='" + id + '\'' +
               ", strand=" + strand() +
               ", " + formatCoordinates() +
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

    public abstract static class Builder<T extends Builder<T>> extends BaseGenomicRegion.Builder<T> {

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

        @Override
        public T with(GenomicVariant genomicVariant) {
            Objects.requireNonNull(genomicVariant, "variant cannot be null");
            return with(genomicVariant.contig(),
                    genomicVariant.id(),
                    genomicVariant.strand(),
                    genomicVariant.coordinates(),
                    genomicVariant.ref(),
                    genomicVariant.alt(),
                    genomicVariant.changeLength(),
                    genomicVariant.mateId(),
                    genomicVariant.eventId());
        }

        public T with(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
            return with(contig, strand, coordinates, ref, requireLengthIfSymbolic(alt), calculateChangeLength(ref, alt));
        }

        public T with(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
            requireLengthIfSymbolic(alt);
            int end = calculateEnd(start, coordinateSystem, ref, alt);
            Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
            return with(contig, strand, coordinates, ref, alt, calculateChangeLength(ref, alt));
        }

        public T with(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
            return with(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
        }

        // keep this private to prevent excessive scoping of this method as that would basically nullify the point of having a builder.
        private T with(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
            super.with(contig, strand, coordinates);
            this.id = Objects.requireNonNull(id);
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = changeLength;
            this.mateId = Objects.requireNonNullElse(mateId, this.mateId);
            this.eventId = Objects.requireNonNullElse(eventId, this.eventId);
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

        @Override
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

        protected abstract BaseGenomicVariant<?> build();

        protected abstract T self();
    }

    private static class AlleleCache {
        
        private static final String A = "A";
        private static final String T = "T";
        private static final String G = "G";
        private static final String C = "C";
        private static final String NO_CALL = ".";
        private static final String SPAN_DEL = "*";
        private static final String N = "N";

        private AlleleCache() {
        }

        private static String cacheAllele(String alt) {
            return alt.length() == 1 ? getCachedBase(alt) : alt;
        }
        
        private static String getCachedBase(String alt) {
            byte base = alt.getBytes(StandardCharsets.UTF_8)[0];
                switch (base) {
                    case 'A':
                        return A;
                    case 'T':
                        return T;
                    case 'G':
                        return G;
                    case 'C':
                        return C;
                    case '.':
                        return NO_CALL;
                    case '*':
                        return SPAN_DEL;
                    case 'N':
                        return N;
                    default:
                        return alt;
                }
            }
    }

    private static class IdCache {

        private static final String MISSING = ".";
        private static final String EMPTY = "";

        private IdCache() {
        }

        /**
         * Returns a cached empty ("") or missing value (".") instance. Nulls will return an empty value. Other
         * identifiers are returned as input.
         *
         * @param id   An identifier string.
         * @return     A cached "" or "." instance or the original input value
         */
        private static String cacheId(String id) {
            if (id == null || id.isEmpty()) {
                return EMPTY;
            }
            if (id.length() == 1 && MISSING.equals(id)) {
                return MISSING;
            }
            return id;
        }
    }
}
