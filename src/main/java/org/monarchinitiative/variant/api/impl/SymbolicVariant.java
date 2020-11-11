package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Represents a large (or small)
 */
public final class SymbolicVariant implements Variant {

    private final Contig contig;
    private final Strand strand;
    private final Position startPosition;
    private final Position endPosition;
    private final String ref;
    private final String alt;
    private final int length;
    private final VariantType variantType;

    public SymbolicVariant(Contig contig, Strand strand, Position startPosition, Position endPosition, String ref, String alt, int length, VariantType variantType) {
        if (!VariantType.isLargeSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create symbolic variant from non-symbolic or breakend allele " + alt);
        }
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
        // check length agrees with start / end type
        this.length = length;
        // check type agrees with alt
        this.variantType = Objects.requireNonNull(variantType);
    }

    /**
     * @return precise one-based, positive strand symbolic variant
     */
    public static SymbolicVariant of(Contig contig, int start, int end, String ref, String alt, int length, VariantType variantType) {
        return of(contig, Position.of(start), Position.of(end), ref, alt, length, variantType);
    }

    /**
     * @return one-based, positive strand symbolic variant
     */
    public static SymbolicVariant of(Contig contig, Position startPosition, Position endPosition, String ref, String alt, int length, VariantType variantType) {
        return new SymbolicVariant(contig, Strand.POSITIVE, startPosition, endPosition, ref, alt, length, variantType);
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public Contig getContig() {
        return contig;
    }

    @Override
    public Position getStartPosition() {
        return startPosition;
    }

    @Override
    public Position getEndPosition() {
        return endPosition;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getAlt() {
        return alt;
    }

    @Override
    public VariantType getType() {
        return variantType;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public SymbolicVariant withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else {
            // TODO - consider actual coordinate systems
            Position start = Position.of(contig.getLength() - startPosition.getPos() + 1,
                    startPosition.getConfidenceInterval().toOppositeStrand());
            // TODO broken with ins/del need to use length which should be +/-
            Position end = Position.of(contig.getLength() - endPosition.getPos() + 1,
                    endPosition.getConfidenceInterval().toOppositeStrand());
            // TODO - start and end should be switched
//            return new SymbolicVariant(contig, strand, start, end, Seq.reverseComplement(ref), alt, length, variantType);
            return new SymbolicVariant(contig, strand, end, start, Seq.reverseComplement(ref), alt, length, variantType);
        }
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
        return length == that.length &&
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
        return Objects.hash(contig, strand, startPosition, endPosition, length, ref, alt, variantType);
    }

    @Override
    public String toString() {
        return "SymbolicVariant{" +
                "contig=" + contig.getId() +
                ", strand=" + strand +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", length=" + length +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType +
                '}';
    }
}
