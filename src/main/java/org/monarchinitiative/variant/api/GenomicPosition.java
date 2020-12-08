package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.DefaultGenomicPosition;
import org.monarchinitiative.variant.api.impl.DefaultGenomicRegion;

/**
 * Represents a {@link Position} on a {@link Contig} and a {@link Strand}. The position is always zero-based for ease
 * of calculations.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicPosition extends Comparable<GenomicPosition>, Stranded<GenomicPosition> {

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

    default int posOneBased() {
        // since GenomicPosition is always in ZERO_BASED, we need to correct the position to get the ONE_BASED position
        return pos() + CoordinateSystem.startDelta(CoordinateSystem.ZERO_BASED, CoordinateSystem.ONE_BASED);
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

    default int distanceTo(GenomicPosition other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());
        }
        other = other.withStrand(strand());
        return other.pos() - pos();
    }


    default int distanceTo(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());

        }
        other = other.withStrand(strand());

        if (other.contains(this)) {
            return 0;
        }

        int s = other.start() - pos();
        int e = other.end() - pos();
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
        return distanceTo(other) > 0;
    }


    /**
     * Test if <code>this</code> position is upstream (5' direction) of the <code>other</code>. The <code>other</code>
     * region is flipped to <code>this</code>'s strand before testing.
     *
     * @param other region to test
     * @return <code>true</code> if <code>this</code> position is upstream of the <code>other</code> region
     */
    default boolean isUpstreamOf(GenomicRegion other) {
        return distanceTo(other) > 0;
    }

    /**
     * Test if <code>this</code> position is downstream (3' direction) of the <code>other</code>. The <code>other</code>
     * position is flipped to <code>this</code>'s strand before testing.
     *
     * @param other position to test
     * @return <code>true</code> if <code>this</code> position is downstream of the <code>other</code> position
     */
    default boolean isDownstreamOf(GenomicPosition other) {
        return distanceTo(other) < 0;
    }

    /**
     * Test if <code>this</code> position is downstream (3' direction) of the <code>other</code>. The <code>other</code>
     * region is flipped to <code>this</code>'s strand before testing.
     *
     * @param other region to test
     * @return <code>true</code> if <code>this</code> position is downstream of the <code>other</code> region
     */
    default boolean isDownstreamOf(GenomicRegion other) {
        return distanceTo(other) < 0;
    }


    /**
     * Convert the position to a 0 bp-long region in {@link CoordinateSystem#ZERO_BASED} coordinate system with precise
     * start/end positions.
     *
     * @return the region
     */
    default GenomicRegion toRegion() {
        return DefaultGenomicRegion.of(contig(), strand(), CoordinateSystem.ZERO_BASED, position().asPrecise(), position().shift(1).asPrecise());
    }

    /**
     * Convert the position to a region in {@link CoordinateSystem#ZERO_BASED} by adding specified number of
     * <code>padding</code> nucleotides upstream and downstream from the position. Length of the returned region
     * is <code>2 * padding</code>. The start/end coordinates of the region are precise.
     *
     * @param padding non-negative number of padding bases to add upstream and downstream from the position
     * @return the padded region
     * @throws IllegalArgumentException if <code>padding < 0</code> or if the padded region would extend
     * the contig boundaries. TODO - check if the exception is really thrown
     */
    default GenomicRegion toRegion(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Cannot apply negative padding: " + padding);
        }
        return toRegion(-padding, padding);
    }

    /**
     * Convert the position to a region in {@link CoordinateSystem#ZERO_BASED} by adding specified number of
     * nucleotides <code>upstream</code> and <code>downstream</code> from the current position. The start/end
     * coordinates of the region are precise.
     * <p>
     * To extend the region in upstream (5') direction, <code>upstream</code> must be <em>negative</em>. To extend the
     * region in downstream direction, <code>downstream</code> must be positive.
     *
     * Length of the returned region is equal to <code>(pos + downstream) - (pos + upstream)</code>, where
     * <code>pos</code> is the current position.
     *
     * @param upstream number of padding bases to add upstream from the position
     * @param downstream number of padding bases to add downstream from the position
     * @return the padded region
     * @throws IllegalArgumentException if <code>downstream < upstream</code> or if the padded region would extend
     * the contig boundaries. TODO - check if the exception is really thrown
     */
    default GenomicRegion toRegion(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return toRegion();
        } else if (upstream >= downstream) {
            throw new IllegalArgumentException("Cannot apply negative padding: " + upstream + ", " + downstream);
        }
        return DefaultGenomicRegion.of(contig(), strand(), CoordinateSystem.ZERO_BASED, position().shift(upstream).asPrecise(), position().shift(downstream).asPrecise());
    }

    @Override
    default int compareTo(GenomicPosition o) {
        return compare(this, o);
    }

    static int compare(GenomicPosition x, GenomicPosition y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Position.compare(x.position(), y.position());
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    /**
     * Create {@link GenomicPosition} starting from a <code>position</code> in <em>one-based</em> coordinate system.
     * <p>
     * Note that the method returns a genomic position in {@link CoordinateSystem#ZERO_BASED}.
     *
     * @return position in {@link CoordinateSystem#ZERO_BASED} coordinate system
     */
    static GenomicPosition oneBased(Contig contig, Strand strand, Position position) {
        return of(contig, strand, CoordinateSystem.ONE_BASED, position);
    }

    /**
     * Create {@link GenomicPosition} starting from a <code>position</code> in <em>zero-based</em> coordinate system.
     *
     * @return position in {@link CoordinateSystem#ZERO_BASED} coordinate system
     */
    static GenomicPosition zeroBased(Contig contig, Strand strand, Position position) {
        return of(contig, strand, CoordinateSystem.ZERO_BASED, position);
    }

    /**
     * Create a {@link CoordinateSystem#ZERO_BASED} genomic position at <code>position</code> in the provided
     * <code>coordinateSystem</code> on <code>contig</code> and <code>strand</code>.
     * <p>
     * For example:
     * <pre>
     * // pos will be 0 in 0-based coordinate system
     * GenomicPosition pos = GenomicPosition.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(1))
     * </pre>
     * <pre>
     * // pos will be 1 in 0-based coordinate system
     * GenomicPosition pos = GenomicPosition.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(1))
     * </pre>
     *
     * @return a position in {@link CoordinateSystem#ZERO_BASED} coordinate system corrected from the input
     * <code>coordinateSystem</code>
     */
    static GenomicPosition of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
        return DefaultGenomicPosition.of(contig, strand, coordinateSystem, position);
    }

}
