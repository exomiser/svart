package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PartialBreakend implements Breakend {

    private final Contig contig;
    private final Position position;
    private final Strand strand;
    private final String seq; // ref and alt isn't applicable here as they are elsewhere

    public PartialBreakend(Contig contig, Position position, Strand strand, String seq) {
        this.contig = Objects.requireNonNull(contig);
        this.position = Objects.requireNonNull(position);
        this.strand = Objects.requireNonNull(strand);
        this.seq = Objects.requireNonNull(seq);
    }

    public static PartialBreakend of(Contig contig, Position position, Strand strand, String seq) {
        return new PartialBreakend(contig, position, strand, seq);
    }

    @Override
    public Contig getContig() {
        return contig;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    // TOD: id and ref might not be required - possible Breakend should not extend GenomicRegion
    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getRef() {
        return seq;
    }

    @Override
    public PartialBreakend withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else {
            Position pos = Position.of(contig.getLength() - position.getPos() + 1,
                    position.getConfidenceInterval().toOppositeStrand());
            return new PartialBreakend(contig, pos, strand, seq);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialBreakend)) return false;
        PartialBreakend that = (PartialBreakend) o;
        return contig.equals(that.contig) &&
                position.equals(that.position) &&
                strand == that.strand &&
                seq.equals(that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, position, strand, seq);
    }

    @Override
    public String toString() {
        return "PartialBreakend{" +
                "contig=" + contig +
                ", position=" + position +
                ", strand=" + strand +
                ", seq='" + seq + '\'' +
                '}';
    }
}
