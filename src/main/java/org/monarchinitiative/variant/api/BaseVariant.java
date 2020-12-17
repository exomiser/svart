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

    private int checkChangeLength(int changeLength, Position endPosition, VariantType variantType) {
        int startZeroBased = normalisedStartPosition(CoordinateSystem.ZERO_BASED).pos();
        if (variantType.baseType() == VariantType.DEL && startZeroBased - (endPosition.pos() - 1) != changeLength) {
            throw new IllegalArgumentException("BAD DEL! ChangeLength:" + changeLength + " does not match expected " + (startZeroBased - (endPosition.pos() - 1) + " given coordinates " + contigId() + " "  + start() + " " + end() + " " + ref + " " + " " + alt));
        } else if (variantType.baseType() == VariantType.INS && (changeLength <= 0)) {
            throw new IllegalArgumentException("BAD INS!");
        } else if (variantType.baseType() == VariantType.DUP && (changeLength <= 0)) {
            throw new IllegalArgumentException("BAD DUP!");
        } else if (variantType.baseType() == VariantType.INV && (changeLength != 0)) {
            throw new IllegalArgumentException("BAD INV!");
        }
        return changeLength;
    }

    protected static int calculateChangeLength(String ref, String alt) {
        return alt.length() - ref.length();
    }

    protected static Position calculateEnd(Position start, CoordinateSystem coordinateSystem, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        // SNV case
        if ((ref.length() | alt.length()) == 1) {
            return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.shift(1) : start;
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
        return newVariantInstance(contig(), id, strand(), coordinateSystem, normalisedStartPosition(coordinateSystem), endPosition(), ref, alt, changeLength);
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
                ", changeLength=" + changeLength +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                '}';
    }

}
