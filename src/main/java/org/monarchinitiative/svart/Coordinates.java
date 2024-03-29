package org.monarchinitiative.svart;


import java.util.Comparator;
import java.util.Objects;

import static org.monarchinitiative.svart.CoordinateSystem.ONE_BASED;
import static org.monarchinitiative.svart.CoordinateSystem.ZERO_BASED;

/**
 * Class providing methods for calculations involving genomic coordinates, where a coordinate is a position in a
 * {@link CoordinateSystem}. This class will handle the conversion of coordinates between coordinate systems such that
 * the user need only provide the coordinates in their original system. For example, it is safe to compare a pair of
 * coordinates for two intervals with different {@link CoordinateSystem}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public sealed interface Coordinates extends Convertible<Coordinates> permits PreciseCoordinates, ImpreciseCoordinates {

    static Coordinates empty() {
        return PreciseCoordinates.EMPTY;
    }

    CoordinateSystem coordinateSystem();

    int start();

    default ConfidenceInterval startConfidenceInterval() {
        return ConfidenceInterval.precise();
    }

    int end();

    default ConfidenceInterval endConfidenceInterval() {
        return ConfidenceInterval.precise();
    }

    Coordinates withCoordinateSystem(CoordinateSystem coordinateSystem);

    default int startWithCoordinateSystem(CoordinateSystem target) {
        return coordinateSystem() == target ? start() : start() + coordinateSystem().startDelta(target);
    }

    default int startZeroBased() {
        return startWithCoordinateSystem(ZERO_BASED);
    }

    default int startOneBased() {
        return startWithCoordinateSystem(ONE_BASED);
    }

    default int endWithCoordinateSystem(CoordinateSystem target) {
        return end();
    }

    default boolean isPrecise() {
        return startConfidenceInterval().isPrecise() && endConfidenceInterval().isPrecise();
    }

    Coordinates asPrecise();

    Coordinates invert(Contig contig);

    default int invertStart(Contig contig) {
        return invertCoordinate(contig, start());
    }

    default int invertEnd(Contig contig) {
        return invertCoordinate(contig, end());
    }

    private int invertCoordinate(Contig contig, int pos) {
        return contig.length() + lengthDelta() - pos;
    }

    /**
     * Inverts the coordinate on the given contig. To be used when transforming coordinates from one strand to the
     * opposite strand. For example if the input coordinate indicates the first base on a contig, the output will return
     * the coordinate of the last base of a contig. This operation is symmetrical and inputting the output of one
     * operation will return the original input. For example given a contig of length 5 and a fully-closed ('1-based')
     * coordinate of 1 (i.e. the first base): invert(1) -> 5, invert(5) -> 1.
     *
     * @param coordinateSystem for the position
     * @param contig           on which the coordinate lies.
     * @param pos              position on the {@link Contig} in the given {@link CoordinateSystem}
     * @return the inverted coordinate on the {@link Contig}
     */
    public static int invertCoordinate(CoordinateSystem coordinateSystem, Contig contig, int pos) {
        return contig.length() + lengthDelta(coordinateSystem) - pos;
    }

    private int lengthDelta() {
        return lengthDelta(coordinateSystem());
    }

    private static int lengthDelta(CoordinateSystem coordinateSystem) {
        return coordinateSystem == ZERO_BASED ? 0 : ZERO_BASED.startDelta(coordinateSystem);
    }

    Coordinates extend(int upstream, int downstream);

    /**
     * Returns the length of a region, in bases
     */
    default int length() {
        // the easiest way to calculate length is to use half-open interval coordinates
        // Why? - See https://www.cs.utexas.edu/users/EWD/transcriptions/EWD08xx/EWD831.html
        // in one and zero-based systems the end is equivalent
        return length(coordinateSystem(), start(), end());
    }

    /**
     * Returns the length of a region, in bases, for the given coordinates.
     *
     * @param coordinateSystem coordinate system for the positions
     * @param start            start coordinate of the region
     * @param end              end coordinate of the region
     * @return length of the region in bases
     */
    public static int length(CoordinateSystem coordinateSystem, int start, int end) {
        // the easiest way to calculate length is to use half-open interval coordinates
        // Why? - See https://www.cs.utexas.edu/users/EWD/transcriptions/EWD08xx/EWD831.html
        return end - zeroBasedStart(coordinateSystem, start);
    }

    default boolean overlaps(Coordinates other) {
        return overlaps(other.coordinateSystem(), other.start(), other.end());
    }

    default boolean overlaps(CoordinateSystem bSystem, int bStart, int bEnd) {
        return overlap(this.coordinateSystem(), this.start(), this.end(), bSystem, bStart, bEnd);
    }

    /**
     * Determines whether two regions overlap, returning true if they do and false if they do not. Empty intervals are
     * NOT considered as overlapping if they are at the boundaries of the other interval. However, two empty intervals
     * with the same start and end coordinates are considered as overlapping. This method is transitive such
     * that overlap(a, b) = overlap(b, a). The input {@link CoordinateSystem} are not required to match.
     *
     * @param aSystem {@link CoordinateSystem} for interval described by positions aStart and aEnd
     * @param aStart  start coordinate of interval a
     * @param aEnd    end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart  start coordinate of interval b
     * @param bEnd    end coordinate of interval b
     * @return true indicating intervals a and b overlap or false if they do not.
     */
    public static boolean overlap(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        if (isEmpty(aSystem, aStart, aEnd) && isEmpty(bSystem, bStart, bEnd)) {
            return zeroBasedStart(aSystem, aStart) == bEnd && zeroBasedStart(bSystem, bStart) == aEnd;
        }
        return zeroBasedStart(aSystem, aStart) < bEnd && zeroBasedStart(bSystem, bStart) < aEnd;
    }

    default int overlapLength(Coordinates other) {
        // in one and zero-based systems the end is equivalent
        return overlapLength(other.coordinateSystem(), other.start(), other.end());
    }

    default int overlapLength(CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd) {
        // in one and zero-based systems the end is equivalent
        return overlapLength(this.coordinateSystem(), this.start(), this.end(), otherCoordinateSystem, otherStart, otherEnd);
    }

    /**
     * Determines the length of overlap between two regions. This method is transitive such
     * that overlapLength(a, b) = overlapLength(b, a). The input {@link CoordinateSystem} are not required to match.
     *
     * @param aSystem {@link CoordinateSystem} for interval described by positions aStart and aEnd
     * @param aStart  start coordinate of interval a
     * @param aEnd    end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart  start coordinate of interval b
     * @param bEnd    end coordinate of interval b
     * @return length of overlap between a and b or zero if there is no overlap.
     */
    public static int overlapLength(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        return Math.max(Math.min(aEnd, bEnd) - Math.max(zeroBasedStart(aSystem, aStart), zeroBasedStart(bSystem, bStart)), 0);
    }

    default boolean contains(Coordinates other) {
        return contains(other.coordinateSystem(), other.start(), other.end());
    }

    default boolean contains(CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd) {
        return aContainsB(this.coordinateSystem(), this.start(), this.end(), otherCoordinateSystem, otherStart, otherEnd);
    }

    /**
     * Determines whether interval a contains b, returning true if they do and false if they do not. Empty interval b
     * is considered as being contained in a if b lies on either boundary of a. The input {@link CoordinateSystem} are
     * not required to match.
     *
     * @param aSystem {@link CoordinateSystem} for interval described by positions aStart and aEnd
     * @param aStart  start coordinate of interval a
     * @param aEnd    end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart  start coordinate of interval b
     * @param bEnd    end coordinate of interval b
     * @return true indicating interval a fully contains b or false if it does not.
     */
    public static boolean aContainsB(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        return zeroBasedStart(aSystem, aStart) <= zeroBasedStart(bSystem, bStart) && bEnd <= aEnd;
    }

    default int distanceTo(Coordinates other) {
        return distanceTo(other.coordinateSystem(), other.start(), other.end());
    }

    default int distanceTo(CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd) {
        return distanceAToB(this.coordinateSystem(), this.start(), this.end(), otherCoordinateSystem, otherStart, otherEnd);
    }

    /**
     * Returns the number of bases present between the intervals <code>a</code> and <code>b</code>. The distance is zero
     * if the <code>a</code> and <code>b</code> are adjacent or if they overlap. The distance is positive if <code>a</code>
     * is upstream (left) of <code>b</code> and negative if <code>a</code> is located downstream (right) of <code>b</code>.
     *
     * @param aSystem {@link CoordinateSystem} for interval described by positions aStart and aEnd
     * @param aStart  start coordinate of interval a
     * @param aEnd    end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart  start coordinate of interval b
     * @param bEnd    end coordinate of interval b
     * @return distance from interval <code>a</code> to interval <code>b</code>
     */
    public static int distanceAToB(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        if (overlap(aSystem, aStart, aEnd, bSystem, bStart, bEnd)) return 0;

        int first = zeroBasedStart(bSystem, bStart) - aEnd;
        int second = zeroBasedStart(aSystem, aStart) - bEnd;

        int result = Math.abs(first) < Math.abs(second) ? first : second;
        return first > second ? result : -result;
    }

    private static boolean isEmpty(CoordinateSystem coordinateSystem, int start, int end) {
        return length(coordinateSystem, start, end) == 0;
    }

    private static int zeroBasedStart(CoordinateSystem coordinateSystem, int start) {
        return coordinateSystem == ZERO_BASED ? start : start - 1;
    }

    /**
     * Returns the required delta to be added to a start position in order to produce an end position in the given
     * {@link CoordinateSystem}. The start position <em>must</em> be in the same coordinate system as provided to this
     * method in order that the correct delta be returned.
     * <p>
     * The end delta value is used, for example, when calculating the end position of a region. Given the coordinate
     * system (C) and a reference allele starting at start position (S) with Length (L), the end position (E) is
     * calculated by adding the delta (D). i.e. E = S + L + D
     * <pre>
     *   C   S  L  E   D
     *   []  1  1  1  -1  (S + L - 1)  ('one-based')
     *   [)  0  1  1   0  (S + L)      ('zero-based')
     * </pre>
     *
     * @param coordinateSystem The coordinateSystem of the required delta
     * @return a delta of -1, 0 or +1 to be added to the start position.
     */
    public static int endDelta(CoordinateSystem coordinateSystem) {
        return coordinateSystem == CoordinateSystem.ONE_BASED ? -1 : 0;
    }

    static void validateCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
        Objects.requireNonNull(coordinateSystem);
        if (end < 0) {
            throw new InvalidCoordinatesException("Coordinates " + start + '-' + end + " cannot have end coordinate `" + end + "` with negative value");
        }
        if (coordinateSystem == CoordinateSystem.ONE_BASED) {
            if (start <= 0) {
                throw new InvalidCoordinatesException("One-based coordinates " + start + '-' + end + " cannot have start coordinate `" + start + "` with zero or negative value");
            }
            if (start > end + 1) {
                // region [2,1] is an empty region, equivalent to (1,2)
                throw new InvalidCoordinatesException("One-based coordinates " + start + '-' + end + " must have a start position at most one place past the end position");
            }
        } else if (coordinateSystem == ZERO_BASED) {
            if (start < 0) {
                throw new InvalidCoordinatesException("Zero-based coordinates " + start + '-' + end + " cannot have start coordinate `" + start + "` with negative value");
            }
            if (start > end) {
                // region [1,1) is an empty region, equivalent to (0,2)
                throw new InvalidCoordinatesException("Zero-based coordinates " + start + '-' + end + " must have a start position before the end position");
            }
        }
    }

    static Coordinates of(CoordinateSystem coordinateSystem, int start, int end) {
        return PreciseCoordinates.of(coordinateSystem, start, end);
    }

    static Coordinates of(CoordinateSystem coordinateSystem, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        if (startCi.isPrecise() && endCi.isPrecise()) {
            return PreciseCoordinates.of(coordinateSystem, start, end);
        }
        return ImpreciseCoordinates.of(coordinateSystem, start, startCi, end, endCi);
    }

    static Coordinates zeroBased(int start, int end) {
        return PreciseCoordinates.of(CoordinateSystem.zeroBased(), start, end);
    }

    static Coordinates oneBased(int start, int end) {
        return PreciseCoordinates.of(CoordinateSystem.oneBased(), start, end);
    }

    static Coordinates zeroBased(int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        return Coordinates.of(CoordinateSystem.zeroBased(), start, startCi, end, endCi);
    }

    static Coordinates oneBased(int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi) {
        return Coordinates.of(CoordinateSystem.oneBased(), start, startCi, end, endCi);
    }

    static Coordinates ofAllele(CoordinateSystem coordinateSystem, int pos, String ref) {
        // Given the coordinate system (C) and a reference allele starting at start position (S) with Length (L) the end
        // position (E) is calculated as:
        //  C   S  L  E
        //  []  1  1  1  (S + L - 1)  ('one-based')
        //  [)  0  1  1  (S + L)      ('zero-based')
        return PreciseCoordinates.of(coordinateSystem, pos, pos + ref.length() + endDelta(coordinateSystem));
    }

    /**
     * Returns a zero-length break at the given coordinate.
     */
    static Coordinates ofBreakend(CoordinateSystem coordinateSystem, int pos, ConfidenceInterval confidenceInterval) {
        Objects.requireNonNull(coordinateSystem);
        return Coordinates.of(coordinateSystem, pos, confidenceInterval, pos + endDelta(coordinateSystem), confidenceInterval);
    }

    static Comparator<Coordinates> naturalOrder() {
        return GenomicComparators.CoordinatesNaturalOrderComparator.INSTANCE;
    }

    static int compare(Coordinates x, Coordinates y) {
        int result = Integer.compare(x.start(), y.startWithCoordinateSystem(x.coordinateSystem()));
        if (result == 0) {
            result = ConfidenceInterval.compare(x.startConfidenceInterval(), y.startConfidenceInterval());
        }
        if (result == 0) {
            result = Integer.compare(x.end(), y.end());
        }
        if (result == 0) {
            result = ConfidenceInterval.compare(x.endConfidenceInterval(), y.endConfidenceInterval());
        }
        return result;
    }

    int hashCode();

    boolean equals(Object o);
}
