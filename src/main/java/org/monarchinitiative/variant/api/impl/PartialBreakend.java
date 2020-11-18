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
    private final String id;

    public PartialBreakend(Contig contig, Position position, Strand strand, String id) {
        this.contig = Objects.requireNonNull(contig);
        this.position = Objects.requireNonNull(position);
        this.strand = Objects.requireNonNull(strand);
        this.id = Objects.requireNonNull(id);
    }

    public static PartialBreakend of(Contig contig, Position position, Strand strand, String id) {
        return new PartialBreakend(contig, position, strand, id);
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    public String id() {
        return "";
    }

    @Override
    public PartialBreakend withStrand(Strand strand) {
        if (this.strand == strand) {
            return this;
        } else {
            Position pos = Position.of(contig.length() - position.pos() + 1,
                    position.confidenceInterval().toOppositeStrand());
            return new PartialBreakend(contig, pos, strand, id);
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
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, position, strand, id);
    }

    @Override
    public String toString() {
        return "PartialBreakend{" +
                "contig=" + contig.id() +
                ", position=" + position +
                ", strand=" + strand +
                ", id='" + id + '\'' +
                '}';
    }
}
