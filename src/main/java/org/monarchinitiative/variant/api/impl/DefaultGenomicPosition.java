package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultGenomicPosition implements GenomicPosition {

    private final Contig contig;
    private final Position position;
    private final Strand strand;

    private DefaultGenomicPosition(Contig contig, Strand strand, Position position) {
        if ((position.minPos() < 0)) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + " that extends beyond first contig base");
        }
        if (position.maxPos() > contig.length()) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + " that extends beyond contig end " + contig.length());
        }

        this.contig = contig;
        this.position = position;
        this.strand = strand;
    }

    public static GenomicPosition of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return new DefaultGenomicPosition(contig, strand, position.shift(coordinateSystem.startDelta(CoordinateSystem.ZERO_BASED)));
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
    public DefaultGenomicPosition withStrand(Strand other) {
        if (strand == other) {
            return this;
        }
        return new DefaultGenomicPosition(contig, other, position.invert(contig, CoordinateSystem.ZERO_BASED));
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGenomicPosition that = (DefaultGenomicPosition) o;
        return Objects.equals(contig, that.contig) &&
                Objects.equals(position, that.position) &&
                strand == that.strand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, position, strand);
    }

    @Override
    public String toString() {
        return "GenomicPosition{" +
                "contig=" + contig +
                ", position=" + position +
                ", strand=" + strand +
                '}';
    }
}
