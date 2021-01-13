package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Represents a large (or small) variant with a 'symbolic' alt allele as defined in the VCF specifications.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class SymbolicVariant implements Variant {

    private final Contig contig;
    private final String id;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position startPosition;
    private final Position endPosition;
    private final String ref;
    private final String alt;
    private final VariantType variantType;
    private final int changeLength;

    private SymbolicVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        if (!VariantType.isLargeSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create symbolic variant from non-symbolic or breakend allele " + alt);
        }
        this.id = Objects.requireNonNull(id);
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);

        int startZeroBased = startWithCoordinateSystem(CoordinateSystem.LEFT_OPEN);
        if (startZeroBased >= endPosition.pos()) {
            throw new IllegalArgumentException("start " + startZeroBased + " must be upstream of end " + endPosition.pos());
        }
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
        this.variantType = VariantType.parseType(alt);
        this.changeLength = checkChangeLength(changeLength, startZeroBased, endPosition, variantType);
    }

    private int checkChangeLength(int changeLength, int startZeroBased, Position endPosition, VariantType variantType) {
        if (variantType.baseType() == VariantType.DEL && (startZeroBased - (endPosition.pos() - 1) != changeLength)) {
            throw new IllegalArgumentException("BAD DEL!");
        } else if (variantType.baseType() == VariantType.INS && (changeLength <= 0)) {
            throw new IllegalArgumentException("BAD INS!");
        } else if (variantType.baseType() == VariantType.DUP && (changeLength <= 0)) {
            throw new IllegalArgumentException("BAD DUP!");
        } else if (variantType.baseType() == VariantType.INV && (changeLength != 0)) {
            throw new IllegalArgumentException("BAD INV!");
        }
        return changeLength;
    }

    /**
     * @return precise one-based, positive strand symbolic variant
     */
    public static SymbolicVariant oneBased(Contig contig, int start, int end, String ref, String alt, int changeLength) {
        return oneBased(contig, "", Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return precise one-based, positive strand symbolic variant
     */
    public static SymbolicVariant oneBased(Contig contig, String id, int start, int end, String ref, String alt, int changeLength) {
        return oneBased(contig, id, Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return one-based, positive strand symbolic variant
     */
    public static SymbolicVariant oneBased(Contig contig, String id, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, startPosition, endPosition, ref, alt, changeLength);
    }

    /**
     * @return precise zero-based, positive strand symbolic variant
     */
    public static SymbolicVariant zeroBased(Contig contig, int start, int end, String ref, String alt, int changeLength) {
        return zeroBased(contig, "", Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    /**
     * @return precise zero-based, positive strand symbolic variant
     */
    public static SymbolicVariant zeroBased(Contig contig, String id, int start, int end, String ref, String alt, int changeLength) {
        return zeroBased(contig, id, Position.of(start), Position.of(end), ref, alt, changeLength);
    }

    public static SymbolicVariant zeroBased(Contig contig, String id, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, startPosition, endPosition, ref, alt, changeLength);
    }

    public static SymbolicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
        return new SymbolicVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Position startPosition() {
        return startPosition;
    }

    @Override
    public Position endPosition() {
        return endPosition;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public Variant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        return new SymbolicVariant(contig, id, strand, coordinateSystem, startPositionWithCoordinateSystem(coordinateSystem), endPositionWithCoordinateSystem(coordinateSystem), ref, alt, changeLength);
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
    public Strand strand() {
        return strand;
    }

    @Override
    public SymbolicVariant withStrand(Strand other) {
        if (strand == other) {
            return this;
        }
        Position start = startPosition.invert(contig, coordinateSystem);
        Position end = endPosition.invert(contig, coordinateSystem);
        return new SymbolicVariant(contig, id, other, coordinateSystem, end, start, Seq.reverseComplement(ref), alt, changeLength);
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SymbolicVariant)) return false;
        SymbolicVariant that = (SymbolicVariant) o;
        return changeLength == that.changeLength &&
                contig.equals(that.contig) &&
                strand == that.strand &&
                startPosition.equals(that.startPosition) &&
                endPosition.equals(that.endPosition) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt) &&
                variantType == that.variantType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, startPosition, endPosition, changeLength, ref, alt, variantType);
    }

    @Override
    public String toString() {
        return "SymbolicVariant{" +
                "contig=" + contig.id() +
                ", strand=" + strand +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", changeLength=" + changeLength +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                '}';
    }
}
