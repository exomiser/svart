package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.GenomicPositionDefault;

/**
 * Represents a {@link Position} on a {@link Contig}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicPosition extends Comparable<GenomicPosition>, Stranded<GenomicPosition>, CoordinateSystemed<GenomicPosition> {

    Contig contig();

    default int contigId() {
        return contig().id();
    }

    default String contigName() {
        return contig().name();
    }

    Position position();

    default int pos() {
        return position().pos();
    }

    default Strand strand() {
        return Strand.POSITIVE;
    }

    default ConfidenceInterval ci() {
        return position().confidenceInterval();
    }

    default int min() {
        return position().minPos();
    }

    default int max() {
        return position().maxPos();
    }

    default int differenceTo(GenomicPosition other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());
        }
        other = other.withStrand(strand()).withCoordinateSystem(coordinateSystem());
        return pos() - other.pos();
    }


    default int differenceTo(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());

        }
        other = other.withStrand(strand()).withCoordinateSystem(coordinateSystem());

        if (other.contains(this)) {
            return 0;
        }

        int s = pos() - other.start();
        int e = pos() - other.end();
        return Math.abs(s) < Math.abs(e) ? s : e;
    }

    /**
     * Test if <code>this</code> position is upstream (5' direction) of the <code>other</code>. The <code>other</code> position is
     * flipped to <code>this</code>'s strand before testing.
     *
     * @param other position to test
     * @return <code>true</code> if <code>this</code> position is upstream of the <code>other</code> position
     */
    default boolean isUpstreamOf(GenomicPosition other) {
        return differenceTo(other) < 0;
    }


    /**
     * Test if <code>this</code> position is upstream (5' direction) of the <code>other</code>. The <code>other</code>
     * region is flipped to <code>this</code>'s strand before testing.
     *
     * @param other region to test
     * @return <code>true</code> if <code>this</code> position is upstream of the <code>other</code> region
     */
    default boolean isUpstreamOf(GenomicRegion other) {
        return differenceTo(other) < 0;
    }

    /**
     * Test if <code>this</code> position is downstream (3' direction) of the <code>other</code>. The <code>other</code>
     * position is flipped to <code>this</code>'s strand before testing.
     *
     * @param other position to test
     * @return <code>true</code> if <code>this</code> position is downstream of the <code>other</code> position
     */
    default boolean isDownstreamOf(GenomicPosition other) {
        return differenceTo(other) > 0;
    }

    /**
     * Test if <code>this</code> position is downstream (3' direction) of the <code>other</code>. The <code>other</code>
     * region is flipped to <code>this</code>'s strand before testing.
     *
     * @param other region to test
     * @return <code>true</code> if <code>this</code> position is downstream of the <code>other</code> region
     */
    default boolean isDownstreamOf(GenomicRegion other) {
        return differenceTo(other) > 0;
    }


    @Override
    default int compareTo(GenomicPosition o) {
        return compare(this, o);
    }

    static int compare(GenomicPosition x, GenomicPosition y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            // TODO: 25. 11. 2020 also consider the coordinate system here?
//            result = Position.compare(x.toZeroBased().position(), y.toZeroBased().position());
            // or this...
            // calculate normalization delta for start positions
            int delta = x.coordinateSystem().delta(y.coordinateSystem());
            result = Position.compare(x.position(), y.position().shift(delta));
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    /**
     * Create genomic position using <em>one-based</em> coordinate system.
     *
     * @return one-based position
     */
    static GenomicPosition oneBased(Contig contig, Strand strand, Position position) {
        return GenomicPositionDefault.oneBased(contig, strand, position);
    }

    /**
     * Create genomic position using <em>zero-based</em> coordinate system.
     *
     * @return zero-based position
     */
    static GenomicPosition zeroBased(Contig contig, Strand strand, Position position) {
        return GenomicPositionDefault.zeroBased(contig, strand, position);
    }

    /**
     * Create genomic position on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code>.
     *
     * @return a position
     */
    static GenomicPosition of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return GenomicPositionDefault.of(contig, strand, coordinateSystem, position);
    }

}
