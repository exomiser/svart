
package org.monarchinitiative.variant.api;


import org.monarchinitiative.variant.api.impl.DefaultGenomicRegion;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicRegion extends Comparable<GenomicRegion>, Stranded<GenomicRegion>, CoordinateSystemed<GenomicRegion> {

    /**
     * @return contig where the region is located
     */
    Contig contig();

    default int contigId() {
        return contig().id();
    }

    default String contigName() {
        return contig().name();
    }

    /**
     * @return start coordinate
     */
    Position startPosition();

    /**
     * @return start coordinate of the region
     */
    default int start() {
        return startPosition().pos();
    }

    /**
     * @return position representing the start border
     */
    default GenomicPosition startGenomicPosition() {
        return GenomicPosition.of(contig(), strand(), CoordinateSystem.ZERO_BASED, normalisedStartPosition(CoordinateSystem.ZERO_BASED));
    }

    default Position normalisedStartPosition(CoordinateSystem coordinateSystem) {
        return startPosition().shift(coordinateSystem().startDelta(coordinateSystem));
    }

    /**
     * The begin position is also the end by default
     *
     * @return end coordinate
     */
    Position endPosition();

    /**
     * @return 1-based end coordinate of the region
     */
    default int end() {
        return endPosition().pos();
    }

    /**
     * @return position representing the end border
     */
    default GenomicPosition endGenomicPosition() {
        // no need to normalize to a coordinate system like the start position, since we never use a coordinate system
        // with open end endpoint
        return GenomicPosition.of(contig(), strand(), CoordinateSystem.ZERO_BASED, endPosition());
    }


    default int length() {
        return end() - start() - coordinateSystem().startDelta(CoordinateSystem.ZERO_BASED);
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ZERO_BASED}, where a region
     * is specified by a half-closed-half-open interval
     */
    @Override
    default boolean isZeroBased() {
        return coordinateSystem().isZeroBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ONE_BASED}, where a region
     * is specified by a closed interval
     */
    @Override
    default boolean isOneBased() {
        return coordinateSystem().isOneBased();
    }

    /**
     * @param other chromosomal region
     * @return true if the region shares at least 1 bp with the <code>other</code> region
     */
    default boolean overlapsWith(GenomicRegion other) {
        if (this.contig().id() != other.contig().id()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(strand());
        return isOneBased()
                ? start() <= onStrand.end() && onStrand.start() <= end()
                : start() < onStrand.end() && onStrand.start() < end();
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicRegion other) {
        if (this.contig().id() != other.contig().id()) {
            return false;
        }
        GenomicRegion onStrand = other.withStrand(strand()).withCoordinateSystem(coordinateSystem());
        return onStrand.start() >= start() && onStrand.end() <= end();
    }

    default boolean contains(GenomicPosition genomicPosition) {
        if (this.contig().id() != genomicPosition.contig().id()) {
            return false;
        }
        GenomicRegion region = withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        int pos = genomicPosition.withStrand(strand()).pos();

        return region.start() <= pos && pos < region.end();
    }

    default GenomicRegion withPadding(int padding) {
        return withPadding(padding, padding);
    }

    default GenomicRegion withPadding(int upstream, int downstream) {
        if (upstream == 0 && downstream == 0) {
            return this;
        }
        return GenomicRegion.of(contig(), strand(), coordinateSystem(), startPosition().shift(-upstream), endPosition().shift(downstream));
    }

    @Override
    default int compareTo(GenomicRegion other) {
        return compare(this, other);
    }

    static int compare(GenomicRegion x, GenomicRegion y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Position.compare(x.startPosition(), y.normalisedStartPosition(x.coordinateSystem()));
        }
        if (result == 0) {
            result = Position.compare(x.endPosition(), y.endPosition());
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    int hashCode();

    boolean equals(Object o);

    /**
     * Create genomic position using <em>one-based</em> coordinate system with <em>precise</em> positions on the
     * <em>forward</em> strand.
     *
     * @return one-based position
     */
    static GenomicRegion oneBased(Contig contig, int startPosition, int endPosition) {
        return DefaultGenomicRegion.oneBased(contig, Strand.POSITIVE, Position.of(startPosition), Position.of(endPosition));
    }

    /**
     * Create genomic position using <em>one-based</em> coordinate system.
     *
     * @return one-based position
     */
    static GenomicRegion oneBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return DefaultGenomicRegion.oneBased(contig, strand, startPosition, endPosition);
    }


    /**
     * Create genomic position using <em>zero-based</em> coordinate system with <em>precise</em> positions on the
     * <em>positive</em> strand.
     *
     * @return zero-based genomic region
     */
    static GenomicRegion zeroBased(Contig contig, int startPosition, int endPosition) {
        return DefaultGenomicRegion.zeroBased(contig, Strand.POSITIVE, Position.of(startPosition), Position.of(endPosition));
    }

    /**
     * Create genomic region using coordinates in <em>zero-based</em> coordinate system.
     *
     * @return zero-based genomic region
     */
    static GenomicRegion zeroBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
        return DefaultGenomicRegion.zeroBased(contig, strand, startPosition, endPosition);
    }

    /**
     * Create a zero-based genomic region from provided {@link GenomicPosition}s.
     *
     * @param start start genomic position
     * @param end   end genomic position
     * @return zero-based genomic region
     */
    static GenomicRegion of(GenomicPosition start, GenomicPosition end) {
        if (start.contig() != end.contig() || start.strand() != end.strand()) {
            throw new IllegalArgumentException("Cannot create a genomic region from positions located on different contigs/strands");
        }
        return of(start.contig(), start.strand(), CoordinateSystem.ZERO_BASED, start.position(), end.position());
    }

    /**
     * Create genomic position on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code>.
     *
     * @return a position
     */
    static GenomicRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return DefaultGenomicRegion.of(contig, strand, coordinateSystem, startPosition, endPosition);
    }
}
