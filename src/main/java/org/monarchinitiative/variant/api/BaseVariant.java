package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.Seq;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class BaseVariant<T extends Variant> extends BaseGenomicRegion<T> implements Variant {

    private final String id;
    private final String ref;
    private final String alt;
    // derived fields
    private final VariantType variantType;
    private final int changeLength;

    protected BaseVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        super(contig, strand, coordinateSystem, startPosition, endPosition);
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ref = Objects.requireNonNull(ref, "ref must not be null");
        this.alt = Objects.requireNonNull(alt, "alt must not be null");
        this.variantType = VariantType.parseType(ref, alt);
        this.changeLength = checkChangeLength(changeLength, endPosition, variantType);
    }

    protected BaseVariant(Builder<?> builder) {
        this(builder.contig, builder.id, builder.strand, builder.coordinateSystem, builder.start, builder.end, builder.ref, builder.alt, builder.changeLength);
    }

    private int checkChangeLength(int changeLength, Position endPosition, VariantType variantType) {
        int startZeroBased = normalisedStart(Endpoint.OPEN);
        if (variantType.baseType() == VariantType.DEL && startZeroBased - (endPosition.pos() - 1) != changeLength) {
            throw new IllegalArgumentException("Illegal DEL changeLength:" + changeLength + ". Does not match expected " + (startZeroBased - (endPosition.pos() - 1) + " given coordinates " + coordinates()));
        } else if (variantType.baseType() == VariantType.INS && (changeLength <= 0)) {
            throw new IllegalArgumentException("Illegal INS changeLength:" + changeLength + ". Should be > 0 given coordinates " + coordinates());
        } else if (variantType.baseType() == VariantType.DUP && (changeLength <= 0)) {
            throw new IllegalArgumentException("Illegal DUP!changeLength:" + changeLength + ". Should be > 0 given coordinates " + coordinates());
        } else if (variantType.baseType() == VariantType.INV && (changeLength != 0) && !isSymbolic()) {
            // symbolic alleles may not be precise, so this can cause failures
            throw new IllegalArgumentException("Illegal INV! changeLength:" + changeLength + ". Should be 0 given coordinates " + coordinates());
        }
        return changeLength;
    }

    private String coordinates() {
        return contigId() + " " + start() + " " + end() + " " + ref + " " + " " + alt;
    }

    protected static int calculateChangeLength(String ref, String alt) {
        return alt.length() - ref.length();
    }

    protected static Position calculateEnd(Position start, CoordinateSystem coordinateSystem, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.withPos(start.pos() + ref.length()) : start.withPos(start.pos() + ref.length() - 1);
    }

    protected static void assertNotBreakend(String alt) {
        if (VariantType.isBreakend(alt)) {
            throw new IllegalArgumentException("Unable to create variant from breakend allele " + alt);
        }
    }

    protected abstract T newVariantInstance(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength);

    @Override
    protected T newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
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
        return newVariantInstance(contig(), id, strand(), coordinateSystem,
                normalisedStartPosition(coordinateSystem.startEndpoint()), normalisedEndPosition(coordinateSystem.endEndpoint()),
                ref, alt, changeLength);
    }

    @Override
    public String id() {
        return id;
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
    @SuppressWarnings("unchecked")
    public T withStrand(Strand other) {
        if (strand() == other) {
            return (T) this;
        }
        Position start = startPosition().invert(contig(), coordinateSystem());
        Position end = endPosition().invert(contig(), coordinateSystem());

        String refRevComp = Seq.reverseComplement(ref);
        String altRevComp = isSymbolic() ? alt : Seq.reverseComplement(alt);
        return newVariantInstance(contig(), id, other, coordinateSystem(), end, start, refRevComp, altRevComp, changeLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseVariant<?> that = (BaseVariant<?>) o;
        return changeLength == that.changeLength && ref.equals(that.ref) && alt.equals(that.alt) && variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ref, alt, variantType, changeLength);
    }

    @Override
    public String toString() {
        return "BaseVariant{" +
                "contig=" + contig().id() +
                ", strand=" + strand() +
                ", coordinateSystem=" + coordinateSystem() +
                ", startPosition=" + startPosition() +
                ", endPosition=" + endPosition() +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                ", length=" + length() +
                ", changeLength=" + changeLength +
                '}';
    }

    protected abstract static class  Builder<T extends Builder<T>> extends BaseGenomicRegion.Builder<T> {

        protected String id = "";
        protected String ref = "";
        protected String alt = "";

        protected int changeLength = 0;

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters so as
        // to maintain the correct state when finally built.

        public T with(Variant variant) {
            Objects.requireNonNull(variant, "variant cannot be null");
            return with(variant.contig(), variant.id(), variant.strand(), variant.coordinateSystem(), variant.startPosition(), variant.endPosition(), variant.ref(), variant.alt(), variant.changeLength());
        }

        public T with(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
            Position end = calculateEnd(start, coordinateSystem, ref, alt);
            int changeLength = calculateChangeLength(ref, alt);
            return with(contig, id, strand, coordinateSystem, start, end, ref, alt, changeLength);
        }

        public T with(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end, String ref, String alt, int changeLength) {
            Objects.requireNonNull(alt);
            assertNotBreakend(alt);
            super.with(contig, strand, coordinateSystem, start, end);
            this.id = Objects.requireNonNull(id);
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            this.changeLength = changeLength;
            return self();
        }

        /**
         * Classes extending this Builder are <em>strongly</em> advised to call this method when implementing the
         * build() method in order to add the missing end coordinates and changeLength if objects have been created
         * using the precise contig-position-ref-alt pattern for non-symbolic variants.
         */
        protected T selfWithEndIfMissing() {
            // should pos be null? 1 is a bit arbitrary.
            if (end.pos() == 1 && changeLength == 0) {
                end = calculateEnd(start, coordinateSystem, ref, alt);
                changeLength = calculateChangeLength(ref, alt);
            }
            return self();
        }

        public T withStrand(Strand strand) {
            if (this.strand == strand) {
                return self();
            }
            this.strand = strand;
            Position invertedStart = start.invert(contig, coordinateSystem);
            start = end.invert(contig, coordinateSystem);
            end = invertedStart;
            ref = Seq.reverseComplement(ref);
            alt = VariantType.isSymbolic(alt) ? alt : Seq.reverseComplement(alt);
            return self();
        }

        protected abstract BaseGenomicRegion<?> build();

        protected abstract T self();
    }

}
