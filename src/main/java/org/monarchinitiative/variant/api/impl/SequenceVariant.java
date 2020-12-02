package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Represents a simple genomic variation of known sequence. Here simple is defined as not being a symbolic and/or
 * breakend re-arrangement.
 */
public class SequenceVariant implements Variant {

    private final Contig contig;
    private final String id;
    private final Strand strand;
    private final CoordinateSystem coordinateSystem;
    private final Position startPosition;
    private final Position endPosition;
    private final String ref;
    private final String alt;

    private final int startZeroBased;

    private SequenceVariant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        this.id = id;
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.coordinateSystem = Objects.requireNonNull(coordinateSystem);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.startZeroBased = coordinateSystem.isZeroBased() ? startPosition.pos() : startPosition.pos() - 1;
        this.endPosition = Objects.requireNonNull(endPosition);
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
    }

    public static SequenceVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        Position end = calculateEnd(start, coordinateSystem, ref, alt);
        return new SequenceVariant(contig, id, strand, coordinateSystem, start, end, ref, alt);
    }

    public static SequenceVariant oneBased(Contig contig, String id, int pos, String ref, String alt) {
        Position start = Position.of(pos);
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt);
    }

    public static SequenceVariant zeroBased(Contig contig, String id, int pos, String ref, String alt) {
        Position start = Position.of(pos);
        return of(contig, id, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, start, ref, alt);
    }

    public static SequenceVariant oneBased(Contig contig, int pos, String ref, String alt) {
        return oneBased(contig, "", pos, ref, alt);
    }

    public static SequenceVariant zeroBased(Contig contig, int pos, String ref, String alt) {
        return zeroBased(contig, "", pos, ref, alt);
    }

    private static Position calculateEnd(Position start, CoordinateSystem coordinateSystem, String ref, String alt) {
        // SNV case
        if ((ref.length() | alt.length()) == 1) {
            return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.shift(1) : start;
        }
        return coordinateSystem == CoordinateSystem.ZERO_BASED ? start.withPos(start.pos() + ref.length()) : start.withPos(start.pos() + ref.length() - 1);
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
    public int startZeroBased() {
        return startZeroBased;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public SequenceVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem == coordinateSystem) {
            return this;
        }
        int startDelta = this.coordinateSystem.delta(coordinateSystem);
        return new SequenceVariant(contig, id, strand, coordinateSystem, startPosition.shift(startDelta), endPosition, ref, alt);
    }

    @Override
    public int changeLength() {
        return alt.length() - ref.length();
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
    public Strand strand() {
        return strand;
    }

    @Override
    public SequenceVariant withStrand(Strand strand) {
        if (!this.strand.needsConversion(strand)) {
            return this;
        }
        if (this.strand.isComplementOf(strand)) {
            Position start = startPosition.invert(contig, coordinateSystem);
            Position end = endPosition.invert(contig, coordinateSystem);
            return new SequenceVariant(contig, id, strand, coordinateSystem, end, start, Seq.reverseComplement(ref), Seq.reverseComplement(alt));
        }
        return new SequenceVariant(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt);
    }

    @Override
    public boolean isSymbolic() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceVariant)) return false;
        SequenceVariant that = (SequenceVariant) o;
        return contig.equals(that.contig) &&
                strand == that.strand &&
                startPosition.equals(that.startPosition) &&
                endPosition.equals(that.endPosition) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, startPosition, endPosition, ref, alt);
    }

    @Override
    public String toString() {
        return "SequenceVariant{" +
                "contig=" + contig.id() +
                ", strand=" + strand +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", length=" + length() +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + variantType() +
                '}';
    }
}
