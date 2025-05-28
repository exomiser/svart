package org.monarchinitiative.svart.interval;

import org.monarchinitiative.svart.*;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Class for creating and querying {@link IntervalTree} spanning multiple {@link Contig}. This is intended to be used as
 * a global index for types of {@link GenomicRegion} in order to enable a simple query of "What genomic regions overlap
 * the query region?".
 *
 * @param <T>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 2.0.0
 */
public final class GenomicIntervalIndex<T extends GenomicInterval> {
    // RIGHT_OPEN, 0-start system
    private static final CoordinateSystem INDEX_COORDINATE_SYSTEM = IntervalTree.COORDINATE_SYSTEM;
    private static final Strand INDEX_STRAND = Strand.POSITIVE;

    private static final GenomicIntervalIndex<?> EMPTY = new GenomicIntervalIndex<>(Map.of());

    private final Map<Contig, IntervalTree<T>> index;

    private GenomicIntervalIndex(Map<Contig, IntervalTree<T>> index) {
        this.index = index;
    }

    /**
     * Static constructor for creating a {@link GenomicIntervalIndex} from a collection of {@link GenomicIntervalIndex}
     * objects of a given type.
     *
     * @param genomicIntervals The {@link GenomicRegion} objects to add to the index
     * @param <T> The type of {@link GenomicRegion} this index contains
     * @return a {@link GenomicIntervalIndex} containing the input {@link GenomicRegion} objects
     */
    public static <T extends GenomicInterval> GenomicIntervalIndex<T> of(Collection<T> genomicIntervals) {
        Map<Contig, List<T>> regionIndex = genomicIntervals.stream()
                .distinct()
                .sorted(GenomicInterval.naturalOrder())
                .collect(groupingBy(T::contig, toList()));

        Map<Contig, IntervalTree<T>> intervalTreeIndex = new HashMap<>();
        for (Map.Entry<Contig, List<T>> entry : regionIndex.entrySet()) {
            intervalTreeIndex.put(entry.getKey(), new IntervalTree<>(entry.getValue(), new GenomicIntervalNormaliser<>()));
        }

        return new GenomicIntervalIndex<>(Map.copyOf(intervalTreeIndex));
    }

    private static class GenomicIntervalNormaliser<T extends GenomicInterval> implements IntervalNormaliser<T> {
        @Override
        public int start(T x) {
            return x.startOnStrandWithCoordinateSystem(INDEX_STRAND, INDEX_COORDINATE_SYSTEM);
        }

        @Override
        public int end(T x) {
            return x.endOnStrandWithCoordinateSystem(INDEX_STRAND, INDEX_COORDINATE_SYSTEM);
        }
    }

    /**
     * Returns an empty index. Useful for testing.
     * @return An empty index
     */
    @SuppressWarnings("unchecked")
    public static <T extends GenomicInterval> GenomicIntervalIndex<T> empty() {
        return (GenomicIntervalIndex<T>) EMPTY;
    }

    /**
     * Searches the index for regions overlapping the query region returning an empty result if there are no other
     * {@link GenomicRegion} on that {@link Contig}, a list of overlapping regions if present or the left and/or right
     * neighbouring {@link GenomicRegion} if there are no overlaps.
     *
     * @param genomicInterval  The {@link GenomicRegion} of interest.
     * @return A list of regions overlapping the given start and end positions.
     */
    public IntervalOverlaps<T> regionsOverlapping(GenomicInterval genomicInterval) {
        IntervalTree<T> intervalTree = index.get(genomicInterval.contig());
        if (intervalTree == null) {
            return IntervalOverlaps.empty();
        }
        int begin = genomicInterval.startOnStrandWithCoordinateSystem(INDEX_STRAND, INDEX_COORDINATE_SYSTEM);
        int end = genomicInterval.endOnStrandWithCoordinateSystem(INDEX_STRAND, INDEX_COORDINATE_SYSTEM);
        return intervalTree.findOverlappingWithInterval(begin, end);
    }

    /**
     * @return the number of genomic regions stored in the index.
     */
    public int size() {
        return index.values().stream().mapToInt(IntervalTree::size).sum();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomicIntervalIndex<?> that = (GenomicIntervalIndex<?>) o;
        return Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}
