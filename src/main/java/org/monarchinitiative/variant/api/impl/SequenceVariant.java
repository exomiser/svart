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
    private final Position startPosition;
    private final Position endPosition;
    private final String ref;
    private final String alt;
    // here ought to be the breakend and adjacency bits too

    public SequenceVariant(Contig contig, String id, Strand strand, Position startPosition, Position endPosition, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        this.id = id;
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
    }

    public static SequenceVariant oneBased(Contig contig, String id, int pos, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        Position start = Position.of(pos, CoordinateSystem.ONE_BASED);
        Position end = calculateEnd(start, ref, alt);
        return new SequenceVariant(contig, id, Strand.POSITIVE, start, end, ref, alt);
    }

    public static SequenceVariant oneBased(Contig contig, int pos, String ref, String alt) {
        return oneBased(contig, "", pos, ref, alt);
    }

    private static Position calculateEnd(Position start, String ref, String alt) {
        // SNV case
        if ((ref.length() | alt.length()) == 1) {
            return start;
        }
        return start.withPos(start.pos() + ref.length() - 1);
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
        if (this.strand == strand) {
            return this;
        } else {
            Position start = Position.of(contig.length() - startPosition.pos() + 1,
                    startPosition.confidenceInterval().toOppositeStrand());
            Position end = Position.of(contig.length() - endPosition.pos() + 1,
                    endPosition.confidenceInterval().toOppositeStrand());
            return new SequenceVariant(contig, id, strand, end, start, Seq.reverseComplement(ref), Seq.reverseComplement(alt));
        }
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
