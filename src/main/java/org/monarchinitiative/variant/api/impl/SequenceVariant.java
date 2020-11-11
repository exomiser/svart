package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Represents a simple genomic variation of known sequence. Here simple is defined as not being a symbolic and/or
 * breakend re-arrangement.
 */
public class SequenceVariant implements Variant {

    private final Contig contig;
    private final Strand strand;
    private final Position startPosition;
    private final Position endPosition;
    private final String ref;
    private final String alt;
    // here ought to be the breakend and adjacency bits too

    public SequenceVariant(Contig contig, Strand strand, Position startPosition, Position endPosition, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        this.contig = Objects.requireNonNull(contig);
        this.strand = Objects.requireNonNull(strand);
        this.startPosition = Objects.requireNonNull(startPosition);
        this.endPosition = Objects.requireNonNull(endPosition);
        this.ref = Objects.requireNonNull(ref);
        this.alt = Objects.requireNonNull(alt);
    }

    public static SequenceVariant of(Contig contig, int pos, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Unable to create non-symbolic variant from symbolic or breakend allele " + alt);
        }
        Position start = Position.of(pos);
        Position end = calculateEnd(start, ref, alt);
        return new SequenceVariant(contig, Strand.POSITIVE, start, end, ref, alt);
    }

    private static Position calculateEnd(Position start, String ref, String alt) {
        // SNV case
        if ((ref.length() | alt.length()) == 1) {
            return start;
        }
        return start.withPos(start.getPos() + ref.length() - 1);
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
    public String getRef() {
        return ref;
    }

    @Override
    public String getAlt() {
        return alt;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public SequenceVariant withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else {
            // TODO - consider coordinate system
            Position start = Position.of(contig.getLength() - startPosition.getPos() + 1,
                    startPosition.getConfidenceInterval().toOppositeStrand());
            Position end = Position.of(contig.getLength() - endPosition.getPos() + 1,
                    endPosition.getConfidenceInterval().toOppositeStrand());
//            return new SequenceVariant(contig, strand, start, end, Seq.reverseComplement(ref), Seq.reverseComplement(alt));
            // TODO - this is how coordinate "withstranding" should be performed, the result is otherwise incorrect
            //  for deletions
            return new SequenceVariant(contig, strand, end, start, Seq.reverseComplement(ref), Seq.reverseComplement(alt));
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
                "contig=" + contig.getId() +
                ", strand=" + strand +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", length=" + getLength() +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", variantType=" + getType() +
                '}';
    }
}
