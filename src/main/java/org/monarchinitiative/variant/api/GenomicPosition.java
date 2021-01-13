package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.DefaultGenomicPosition;
import org.monarchinitiative.variant.api.impl.DefaultGenomicRegion;

import java.util.Comparator;

import static org.monarchinitiative.variant.api.GenomicComparators.*;

/**
 * Represents a {@link Position} on a {@link Contig} and a {@link Strand}. The position is always zero-based for ease
 * of calculations.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicPosition extends Position, Stranded<GenomicPosition> {

    Contig contig();

    default int contigId() {
        return contig().id();
    }

    default String contigName() {
        return contig().name();
    }

    default Strand strand() {
        return Strand.POSITIVE;
    }

    default int distanceTo(GenomicPosition other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());
        }
        return distanceTo(other.withStrand(strand()).pos());
    }


    default int distanceTo(GenomicRegion other) {
        if (contigId() != other.contigId())
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());

        return distanceTo((Region) other.withStrand(strand()));
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
     * Convert the position to a 0 bp-long region in {@link CoordinateSystem#LEFT_OPEN} coordinate system with precise
     * start/end positions.
     *
     * @return the region
     */
    default GenomicRegion toRegion() {
        return DefaultGenomicRegion.of(contig(), strand(), CoordinateSystem.LEFT_OPEN,
                Position.of(pos() - 1), Position.of(pos()));
    }

    /**
     * Convert the position to a region in {@link CoordinateSystem#LEFT_OPEN} by adding specified number of
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
     * Convert the position to a region in {@link CoordinateSystem#LEFT_OPEN} by adding specified number of
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

        return DefaultGenomicRegion.of(contig(), strand(), CoordinateSystem.zeroBased(),
                Position.of(pos() + upstream),
                Position.of(pos() + downstream));
    }

    static Comparator<GenomicPosition> naturalOrder() {
        return GenomicPositionNaturalOrderComparator.INSTANCE;
    }

    static int compare(GenomicPosition x, GenomicPosition y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Position.compare(x, y);
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    /**
     * Create {@link GenomicPosition} starting from a <code>position</code> in <em>one-based</em> coordinate system.
     * <p>
     * Note that the method returns a genomic position in {@link CoordinateSystem#LEFT_OPEN}.
     *
     * @return position in {@link CoordinateSystem#LEFT_OPEN} coordinate system
     */
    @Deprecated // we should not think of positions as zero or one based anymore
    static GenomicPosition oneBased(Contig contig, Strand strand, Position position) {
        return of(contig, strand, position);
    }

    /**
     * Create {@link GenomicPosition} starting from a <code>position</code> in <em>zero-based</em> coordinate system.
     *
     * @return position in {@link CoordinateSystem#LEFT_OPEN} coordinate system
     */
    @Deprecated // we should not think of positions as zero or one based anymore
    static GenomicPosition zeroBased(Contig contig, Strand strand, Position position) {
        return of(contig, strand, position);
    }

    /**
     * Create a {@link CoordinateSystem#LEFT_OPEN} genomic position at <code>position</code> in the provided
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
     * @return a position in {@link CoordinateSystem#LEFT_OPEN} coordinate system corrected from the input
     * <code>coordinateSystem</code>
     */
    static GenomicPosition of(Contig contig, Strand strand, Position position) {
        return DefaultGenomicPosition.of(contig, strand, position);
    }

}
