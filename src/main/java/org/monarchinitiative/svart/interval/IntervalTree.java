package org.monarchinitiative.svart.interval;

import org.monarchinitiative.svart.CoordinateSystem;

import java.util.*;


/**
 * Sorted array of {@link IntervalTreeNode} objects representing an immutable interval
 * tree.
 * <p>
 * The query results are sorted lexicographically by <code>(begin, end)</code>.
 * <p>
 * Taken from Jannovar, with minor alterations.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @since 2.0.0
 */
public final class IntervalTree<T> {

    public static final CoordinateSystem COORDINATE_SYSTEM = CoordinateSystem.ZERO_BASED;

    /**
     * list of {@link IntervalTreeNode} objects, sorted by begin position
     */
    private final List<IntervalTreeNode<T>> nodes;

    /**
     * list of {@link IntervalTreeNode} objects, sorted by end position
     */
    private final List<IntervalTreeNode<T>> intervalsEnd;

    /**
     * Construct object with the given values.
     */
    public IntervalTree(Collection<T> elements, IntervalNormaliser<T> intervalNormaliser) {
        IntervalListBuilder<T> pair = new IntervalListBuilder<>(elements, intervalNormaliser);
        this.nodes = pair.nodes;
        this.intervalsEnd = pair.intervalsEnd;
    }

    /**
     * @return {@link IntervalTreeNode}s, sorted by begin position
     */
    public List<IntervalTreeNode<T>> intervalsByStart() {
        return nodes;
    }

    /**
     * @return {@link IntervalTreeNode}s, sorted by end position
     */
    public List<IntervalTreeNode<T>> intervalsByEnd() {
        return intervalsEnd;
    }

    /**
     * @return the number of elements in the tree
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Query the encoded interval tree for all values with intervals overlapping
     * with a given <code>point</code>.
     *
     * @param point zero-based point for the query
     * @return the elements from the intervals overlapping with the point
     * <code>point</code>
     */
    public IntervalOverlaps<T> findOverlappingWithPoint(int point) {
        List<T> overlapping = new ArrayList<>();
        findOverlappingWithPoint(0, nodes.size(), nodes.size() / 2, point, overlapping);

        // if overlapping interval was found then return this set
        if (!overlapping.isEmpty()) {
            return IntervalOverlaps.of(overlapping);
        }

        // otherwise, find left and right neighbour
        T left = findLeftNeighbor(point);
        T right = findRightNeighbor(point);
        return IntervalOverlaps.neighbours(left, right);
    }

    /**
     * @return right neighbor of the given point if any, or <code>null</code>
     */
    private T findRightNeighbor(int point) {
        final IntervalTreeNode<T> query = new IntervalTreeNode<>(point, point, null, point);
        int idx = Collections.binarySearch(nodes, query, Comparator.comparingInt(IntervalTreeNode::begin));

        if (idx >= 0) {
            throw new IllegalStateException("Found element although in right neighbor search!");
        }
        idx = -(idx + 1); // convert to insertion point

        if (idx == nodes.size()) {
            return null;
        }
        return nodes.get(idx).value();
    }

    /**
     * @return left neighbor of the given point if any, or <code>null</code>
     */
    private T findLeftNeighbor(int point) {
        final IntervalTreeNode<T> query = new IntervalTreeNode<>(point, point, null, point);
        int idx = Collections.binarySearch(intervalsEnd, query, Comparator.comparingInt(IntervalTreeNode::end));

        if (idx >= 0) {
            idx += 1;
        } else {
            idx = -(idx + 1); // convert to insertion point
        }

        if (idx == 0) {
            return null;
        }
        return intervalsEnd.get(idx - 1).value();
    }

    /**
     * Implementation of in-order traversal of the encoded tree with pruning using {@link IntervalTreeNode#maxEnd()}.
     *
     * @param begin       begin index of subtree to search through
     * @param end         end index of subtree to search through
     * @param center      root index of subtree to search through
     * @param point       point to use for querying
     * @param overlapping {@link List} to add values to
     */
    private void findOverlappingWithPoint(int begin, int end, int center, int point, List<T> overlapping) {
        // handle base case of empty interval
        if (begin >= end) {
            return;
        }

        IntervalTreeNode<T> node = nodes.get(center); // shortcut to current node

        // point is right of the rightmost point of any interval in this node
        if (node.allLeftOf(point)) {
            return;
        }

        // recurse left
        if (begin < center) {
            findOverlappingWithPoint(begin, center, begin + (center - begin) / 2, point, overlapping);
        }

        // check this node
        if (node.contains(point)) {
            overlapping.add(node.value());
        }

        // point is left of the start of the interval, can't to the right
        if (node.isRightOf(point)) {
            return;
        }

        // recurse right
        if (center + 1 < end) {
            findOverlappingWithPoint(center + 1, end, (center + 1) + (end - (center + 1)) / 2, point, overlapping);
        }
    }

    /**
     * Query the encoded interval tree for all values with intervals overlapping with a given <code>interval</code>.
     *
     * @param begin zero-based begin position of the query interval
     * @param end   zero-based end position of the query interval
     * @return the elements from the intervals overlapping with the interval
     * <code>[begin, end)</code>
     */
    public IntervalOverlaps<T> findOverlappingWithInterval(int begin, int end) {
        List<T> overlapping = new ArrayList<>();
        findOverlappingWithInterval(0, nodes.size(), nodes.size() / 2, begin, end, overlapping);

        // if overlapping interval was found then return this set
        if (!overlapping.isEmpty()) {
            return IntervalOverlaps.of(overlapping);
        }

        // otherwise, find left and right neighbour, can use begin for all queries, have no overlap
        T left = findLeftNeighbor(begin);
        T right = findRightNeighbor(begin);
        return IntervalOverlaps.neighbours(left, right);
    }

    /**
     * Implementation of in-order traversal of the encoded tree with pruning using {@link IntervalTreeNode#maxEnd()}.
     *
     * @param begin       begin index of subtree to search through
     * @param end         end index of subtree to search through
     * @param center      root index of subtree to search through
     * @param iBegin      interval begin to use for querying
     * @param iEnd        interval end to use for querying
     * @param overlapping {@link List} to add values overlapping the interval to
     */
    private void findOverlappingWithInterval(int begin, int end, int center, int iBegin, int iEnd, List<T> overlapping) {
        // handle base case of empty interval
        if (begin >= end) {
            return;
        }

        // shortcut to current node
        IntervalTreeNode<T> node = nodes.get(center);

        // iBegin is right of the rightmost point of any interval in this node
        if (node.allLeftOf(iBegin)) {
            return;
        }

        // recurse left
        if (begin < center) {
            findOverlappingWithInterval(begin, center, begin + (center - begin) / 2, iBegin, iEnd, overlapping);
        }

        // check this node
        if (node.overlapsWith(iBegin, iEnd)) {
            overlapping.add(node.value());
        }

        // last interval entry is left of the start of the interval, can't to the right
        if (node.isRightOf(iEnd - 1)) {
            return;
        }

        // recurse right
        if (center + 1 < end) {
            findOverlappingWithInterval(center + 1, end, (center + 1) + (end - (center + 1)) / 2, iBegin, iEnd, overlapping);
        }
    }

    /**
     * Helper class for building the interval lists.
     */
    private static class IntervalListBuilder<T> {

        private final List<IntervalTreeNode<T>> nodes;
        private final List<IntervalTreeNode<T>> intervalsEnd;

        public IntervalListBuilder(Collection<T> elements, IntervalNormaliser<T> intervalNormaliser) {
            List<MutableInterval<T>> mutableIntervals = buildMutableIntervals(elements, intervalNormaliser);
            this.nodes = toIntervalList(mutableIntervals);
            this.intervalsEnd = buildIntervalsEnd(mutableIntervals);
        }

        private List<MutableInterval<T>> buildMutableIntervals(Collection<T> elements, IntervalNormaliser<T> intervalNormaliser) {
            // obtain list of elements sorted by begin positions
            List<MutableInterval<T>> tmpList = new ArrayList<>(elements.size());
            for (T element : elements) {
                tmpList.add(new MutableInterval<>(intervalNormaliser.start(element), intervalNormaliser.end(element), element));
            }
            Collections.sort(tmpList);
            // compute the maxEnd members of the lst entries
            computeMaxEnds(tmpList, 0, tmpList.size());
            return tmpList;
        }

        private int computeMaxEnds(List<MutableInterval<T>> lst, int beginIdx, int endIdx) {
            if (beginIdx == endIdx) {
                return -1;
            }

            int centerIdx = (endIdx + beginIdx) / 2;
            MutableInterval<T> mi = lst.get(centerIdx);

            if (beginIdx + 1 == endIdx) {
                return mi.getMaxEnd();
            }

            mi.setMaxEnd(Math.max(mi.getMaxEnd(),
                    Math.max(computeMaxEnds(lst, beginIdx, centerIdx),
                            computeMaxEnds(lst, centerIdx + 1, endIdx))));
            return mi.getMaxEnd();
        }

        /**
         * Fill {@link #intervalsEnd}.
         */
        private List<IntervalTreeNode<T>> buildIntervalsEnd(List<MutableInterval<T>> tmpList) {
            // sort by (end, begin)
            tmpList.sort(this::compareEndBegin);
            return toIntervalList(tmpList);
        }

        private List<IntervalTreeNode<T>> toIntervalList(List<MutableInterval<T>> tmpList) {
            List<IntervalTreeNode<T>> intervalTreeNodes = new ArrayList<>(tmpList.size());
            for (MutableInterval<T> i : tmpList) {
                intervalTreeNodes.add(new IntervalTreeNode<>(i.getBegin(), i.getEnd(), i.getValue(), i.getMaxEnd()));
            }
            return List.copyOf(intervalTreeNodes);
        }

        private int compareEndBegin(MutableInterval<T> o1, MutableInterval<T> o2) {
            final int result = o1.getEnd() - o2.getEnd();
            if (result == 0) {
                return o1.getBegin() - o2.getBegin();
            }
            return result;
        }
    }

}
