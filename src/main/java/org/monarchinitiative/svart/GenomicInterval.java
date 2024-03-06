package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultGenomicInterval;

import java.util.Comparator;

/**
 * A {@link GenomicInterval} is an {@link Interval} on a {@link Strand} of a {@link Contig}. e.g. transcribed, regulatory
 * or intergenic regions of a chromosome.
 * <p>
 * For regions requiring the ability to be transposed or converted from one {@link Strand} or {@link CoordinateSystem} to
 * another, the {@link Transposable} and/or {@link Convertible} or the {@link GenomicRegion} interfaces should be
 * implemented. However, the {@link GenomicInterval} contains methods such as {@code startOnStrand} and
 * {@code startOnStrandWithCoordinateSystem} which allows common operations for comparison of {@link GenomicInterval}s
 * without the requirement to create a new object first.
 * <p>
 * Note that for classes implementing this interface, it is <b>strongly</b> recommended that they call the
 * validateCoordinatesOnContig method at construction time in order to avoid errors later on.
 */
public interface GenomicInterval extends Stranded, Interval {

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
     * Ensures that the coordinates fit within the length of the contig. It is <b>strongly</b> recommended that classes
     * implementing {@link GenomicInterval} use this method to validate their inputs.
     *
     * @param contig      {@link Contig} on which the interval is located
     * @param coordinates {@link Coordinates} of the interval.
     * @throws CoordinatesOutOfBoundsException when the coordinates overflow the length of the contig.
     */
    static void validateCoordinatesOnContig(Contig contig, Coordinates coordinates) {
        validateCoordinatesOnContig(contig, coordinates.coordinateSystem(), coordinates.start(), coordinates.end());
    }

    /**
     * Ensures that the coordinates fit within the length of the contig. It is <b>strongly</b> recommended that classes
     * implementing {@link GenomicInterval} use this method to validate their inputs.
     *
     * @param contig           {@link Contig} on which the interval is located
     * @param coordinateSystem {@link CoordinateSystem} of the interval.
     * @param start            interval start
     * @param end              interval end
     * @throws CoordinatesOutOfBoundsException when the coordinates overflow the length of the contig.
     */
    static void validateCoordinatesOnContig(Contig contig, CoordinateSystem coordinateSystem, int start, int end) {
        if (coordinateSystem == CoordinateSystem.ONE_BASED && (start < 1 || end > contig.length())) {
            throw new CoordinatesOutOfBoundsException("One-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 1 + ',' + contig.length() + ']');
        } else if (coordinateSystem == CoordinateSystem.ZERO_BASED && (start < 0 || end > contig.length())) {
            throw new CoordinatesOutOfBoundsException("Zero-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 0 + ',' + contig.length() + ')');
        }
    }

    /**
     * @param other chromosomal region
     * @return true if the interval shares at least 1 bp with the <code>other</code> interval
     */
    default boolean overlapsWith(GenomicInterval other) {
        if (this.contigId() != other.contigId()) {
            return false;
        }
        if (this.strand() == other.strand()) {
            return this.coordinates().overlaps(other.coordinates());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return this.coordinates().overlaps(other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the length of overlap between this region and another. {@link GenomicInterval}s not overlapping or on
     * another {@link Contig} will return a length of zero. This method will automatically correct for different
     * {@link CoordinateSystem} and {@link Strand}. Note that if the {@link GenomicInterval} are on different strands, the
     * return value will be computed <em>as if they were on the same strand</em>.
     *
     * @param other the other {@link GenomicInterval} to measure the overlap with
     * @return the length of overlap in bases or zero if no overlap.
     */
    default int overlapLength(GenomicInterval other) {
        if (this.contigId() != other.contigId()) {
            return 0;
        }
        if (this.strand() == other.strand()) {
            return this.coordinates().overlapLength(other.coordinates());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return this.coordinates().overlapLength(other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the start coordinate for this interval on the given strand. The resulting coordinate will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} on which the start coordinate is required
     * @return start coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int startOnStrand(Strand strand) {
        return this.strand() == strand ? start() : coordinates().invertEnd(contig());
    }

    /**
     * Returns the start coordinate for this interval on the specified strand and coordinate coordinateSystem.
     *
     * @param strand           target {@link Strand} on which the start coordinate is required
     * @param coordinateSystem target {@link CoordinateSystem} in which the start coordinate is required
     * @return start coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int startOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem) {
        return (this.strand() == strand && this.coordinateSystem() == coordinateSystem) ? start() : startOnStrand(strand) + coordinateSystem().startDelta(coordinateSystem);
    }

    /**
     * Returns the start coordinate of the interval in Genomic Standard Coordinates (zero-start, right open) as used in
     * BED or UCSC server. This is suitable for use when performing transformations, distance calculations or comparisons
     * between {@link GenomicInterval} objects in absolute or reference genomic space.
     * <p>
     * Recommended for standard use by the <a href=https://genomestandards.org/standards/genome-coordinates/>GA4GH GKS workstream</a>.
     *
     * @return the start coordinate on the positive strand using zero-based coordinates.
     */
    default int startStd() {
        return startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED);
    }

    /**
     * Returns the start coordinate of the interval in zero-based coordinates on the strand specified. This is suitable
     * for use when performing transformations, distance calculations or comparisons between {@link GenomicInterval}
     * objects on a particular strand.
     *
     * @param strand The {@link Strand} on which to return the coordinate on.
     * @return the start coordinate on the specified strand using zero-based coordinates.
     */
    default int startZeroBased(Strand strand) {
        return startOnStrandWithCoordinateSystem(strand, CoordinateSystem.ZERO_BASED);
    }

    /**
     * Returns the start coordinate of the interval in one-based coordinates on the strand specified. This is suitable
     * for use when displaying variant information to humans as used in web-based genome browsers such as Ensembl, and
     * compatibility with SAM, VCF, GFF, Wiggle and HGVS formats.
     *
     * @param strand The {@link Strand} on which to return the coordinate on.
     * @return the start coordinate on the specified strand using one-based coordinates.
     */
    default int startOneBased(Strand strand) {
        return startOnStrandWithCoordinateSystem(strand, CoordinateSystem.ONE_BASED);
    }

    /**
     * Returns the end coordinate of the interval in one-based coordinates on the strand specified. This is suitable
     * for use when displaying variant information to humans as used in web-based genome browsers such as Ensembl, and
     * compatibility with SAM, VCF, GFF, Wiggle and HGVS formats.
     *
     * @param strand The {@link Strand} on which to return the coordinate on.
     * @return the end coordinate on the specified strand using one-based coordinates.
     */
    default int endOneBased(Strand strand) {
        return endOnStrandWithCoordinateSystem(strand, CoordinateSystem.ONE_BASED);
    }

    /**
     * Returns the end coordinate for this region on the given strand. The resulting coordinate will be given in the
     * coordinates indicated by the coordinateSystem of the object instance.
     *
     * @param strand target {@link Strand} for which the end coordinate is required
     * @return end coordinate in the {@link CoordinateSystem} of the object on the designated {@link Strand}
     */
    default int endOnStrand(Strand strand) {
        return this.strand() == strand ? end() : coordinates().invertStart(contig());
    }

    /**
     * Returns the end coordinate for this interval on the specified strand and coordinate system.
     *
     * @param strand           the target strand on which the end coordinate is required
     * @param coordinateSystem the target coordinate system in which the end coordinate is required
     * @return the end coordinate in the specified coordinate system on the designated strand,
     * or the end coordinate on the current strand if the strand and coordinate system match
     */
    default int endOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem) {
        return this.strand() == strand && this.coordinateSystem() == coordinateSystem ? end() : endOnStrand(strand);
    }

    /**
     * Returns the end coordinate of the interval in Genomic Standard Coordinates (zero-start, right open) as used in
     * BED or UCSC server. This is suitable for use when performing transformations, distance calculations or comparisons
     * between {@link GenomicInterval} objects in absolute or reference genomic space.
     * <p>
     * Recommended for standard use by the <a href=https://genomestandards.org/standards/genome-coordinates/>GA4GH GKS
     * workstream</a>.
     *
     * @return the end coordinate on the positive strand using zero-based coordinates.
     */
    default int endStd() {
        return endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED);
    }

    /**
     * Returns the end coordinate of the interval in zero-based coordinates on the strand specified. This is suitable
     * for use when performing transformations, distance calculations or comparisons between {@link GenomicInterval}
     * objects on a particular strand.
     *
     * @param strand The {@link Strand} on which to return the coordinate on.
     * @return the end coordinate on the specified strand using zero-based coordinates.
     */
    default int endZeroBased(Strand strand) {
        return endOnStrandWithCoordinateSystem(strand, CoordinateSystem.ZERO_BASED);
    }

    /**
     * @param other chromosomal region
     * @return true if the <code>other</code> region is fully contained within this region
     */
    default boolean contains(GenomicInterval other) {
        if (this.contig().id() != other.contig().id()) {
            return false;
        }
        if (this.strand() == other.strand()) {
            return this.coordinates().contains(other.coordinates());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return this.coordinates().contains(other.coordinateSystem(), otherStart, otherEnd);
    }

    /**
     * Returns the distance between <code>this</code> and the <code>other</code> regions. The distance represents
     * the number of bases present between the regions.
     * <p>
     * The distance is zero if the <code>a</code> and <code>b</code>
     * are adjacent or if they overlap. The distance is positive if <code>a</code> is upstream of <code>b</code>
     * and negative if <code>a</code> is located downstream from <code>b</code>.
     *
     * @param other genomic region
     * @return distance from <code>this</code> region to the <code>other</code> region
     */
    default int distanceTo(GenomicInterval other) {
        if (this.contig().id() != other.contig().id()) {
            throw new IllegalArgumentException("Cannot calculate distance between regions on different contigs: " + contig().id() + " <-> " + other.contig().id());
        }
        if (this.strand() == other.strand()) {
            return this.coordinates().distanceTo(other.coordinates());
        }
        int otherStart = other.startOnStrand(this.strand());
        int otherEnd = other.endOnStrand(this.strand());
        return this.coordinates().distanceTo(other.coordinateSystem(), otherStart, otherEnd);
    }


    static Comparator<GenomicInterval> naturalOrder() {
        return GenomicComparators.GenomicIntervalNaturalOrderComparator.INSTANCE;
    }

    static int compare(GenomicInterval x, GenomicInterval y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Interval.compare(x, y);
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }
        return result;
    }

    int hashCode();

    boolean equals(Object o);


    /**
     * Create genomic interval on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code> with
     * precise start and end coordinates
     *
     * @return a genomic interval
     */
    static GenomicInterval of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end) {
        return of(contig, strand, Coordinates.of(coordinateSystem, start, end));
    }

    /**
     * Create genomic interval on <code>contig</code> and <code>strand</code> using <code>coordinateSystem</code> with
     * imprecise start and end coordinates
     *
     * @return a genomic interval
     */
    static GenomicInterval of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, startCi, end, endCi);
        return of(contig, strand, coordinates);
    }

    /**
     * Create genomic interval on <code>contig</code> and <code>strand</code> with <code>coordinates</code>.
     *
     * @return a genomic interval
     */
    static GenomicInterval of(Contig contig, Strand strand, Coordinates coordinates) {
        return DefaultGenomicInterval.of(contig, strand, coordinates);
    }
}
