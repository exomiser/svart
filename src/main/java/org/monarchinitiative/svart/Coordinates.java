package org.monarchinitiative.svart;


import java.util.Objects;

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
public interface Coordinates extends CoordinateSystemed<Coordinates> {

    Coordinates EMPTY = PreciseCoordinates.of(ZERO_BASED, 0, 0);

    default Coordinates empty() {
        return EMPTY;
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
        return start() + coordinateSystem().startDelta(target);
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

    private int lengthDelta() {
        return coordinateSystem() == ZERO_BASED ? 0 : ZERO_BASED.startDelta(coordinateSystem());
    }

    default int length() {
        // the easiest way to calculate length is to use half-open interval coordinates
        // Why? - See https://www.cs.utexas.edu/users/EWD/transcriptions/EWD08xx/EWD831.html
        // in one and zero-based systems the end is equivalent
        return end() - openStart();
    }

    default int overlapLength(Coordinates other) {
        // in one and zero-based systems the end is equivalent
        // in one and zero-based systems the end is equivalent
        return Math.max(Math.min(end(), other.end()) - Math.max(this.openStart(), other.openStart()), 0);
    }

    default int distanceTo(Coordinates other) {
        if (this.overlaps(other)) return 0;

        // in one and zero-based systems the end is equivalent
        int first = other.openStart() - end();
        // in one and zero-based systems the end is equivalent
        int second = this.openStart() - other.end();

        int result = Math.abs(first) < Math.abs(second) ? first : second;
        return first > second ? result : -result;
    }

    default boolean overlaps(Coordinates other) {
        // Check empty intervals abutting a region are included, this includes other empty intervals at the same position.
        if (this.isEmpty()) {
            return other.contains(this);
        }
        if (other.isEmpty()) {
            return this.contains(other);
        }
        // in one and zero-based systems the end is equivalent
        // in one and zero-based systems the end is equivalent
        return this.openStart() < other.end() && other.openStart() < end();
    }

    default boolean overlaps(CoordinateSystem bSystem, int bStart, int bEnd) {
        // Check empty intervals abutting a region are included, this includes other empty intervals at the same position.
        if (this.isEmpty()) {
            return aContainsB(bSystem, bStart, bEnd, this.coordinateSystem(), this.start(), this.end());
        }
        if (isEmpty(bSystem, bStart, bEnd)) {
            return aContainsB(this.coordinateSystem(), this.start(), this.end(), bSystem, bStart, bEnd);
        }
        return this.openStart() < bEnd && openStart(bSystem, bStart) < end();
    }

    private boolean isEmpty() {
        return length() == 0;
    }

    default boolean contains(Coordinates other) {
        return this.openStart() <= other.openStart() && other.end() <= end();
    }

    default boolean contains(int position) {
        return startWithCoordinateSystem(CoordinateSystem.ONE_BASED) <= position && position <= endWithCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    private int openStart() {
        return coordinateSystem().startBound() == Bound.OPEN ? start() : start() - 1;
    }

    Coordinates withPadding(int upstream, int downstream);

    static int compare(Coordinates x, Coordinates y) {
        int result = Integer.compare(x.start(), y.startWithCoordinateSystem(x.coordinateSystem()));
        if (result == 0) {
            result = ConfidenceInterval.compare(x.startConfidenceInterval(), y.startConfidenceInterval());
        }
        if (result == 0) {
            result = Integer.compare(x.end(), y.endWithCoordinateSystem(x.coordinateSystem()));
        }
        if (result == 0) {
            result = ConfidenceInterval.compare(x.endConfidenceInterval(), y.endConfidenceInterval());
        }
        return result;
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

    int hashCode();

    boolean equals(Object o);

    default void validate() {
        validateCoordinates(coordinateSystem(), start(), end());
    }

    default void validateOnContig(Contig contig) {
        validateOnContig(contig, this);
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
        return end - openStart(coordinateSystem, start);
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
    public static int invertPosition(CoordinateSystem coordinateSystem, Contig contig, int pos) {
        return contig.length() + lengthDelta(coordinateSystem) - pos;
    }

    private static int lengthDelta(CoordinateSystem coordinateSystem) {
        return coordinateSystem == ZERO_BASED ? 0 : ZERO_BASED.startDelta(coordinateSystem);
    }

    /**
     * Determines whether two regions overlap, returning true if they do and false if they do not. Empty intervals are
     * considered as overlapping if they are at the boundaries of the other interval. This method is transitive such
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
        // Check empty intervals abutting a region are included, this includes other empty intervals at the same position.
        if (isEmpty(aSystem, aStart, aEnd)) {
            return aContainsB(bSystem, bStart, bEnd, aSystem, aStart, aEnd);
        }
        if (isEmpty(bSystem, bStart, bEnd)) {
            return aContainsB(aSystem, aStart, aEnd, bSystem, bStart, bEnd);
        }
        return openStart(aSystem, aStart) < bEnd && openStart(bSystem, bStart) < aEnd;
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
        return Math.max(Math.min(aEnd, bEnd) - Math.max(openStart(aSystem, aStart), openStart(bSystem, bStart)), 0);
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
        return openStart(aSystem, aStart) <= openStart(bSystem, bStart) && bEnd <= aEnd;
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

        int first = openStart(bSystem, bStart) - aEnd;
        int second = openStart(aSystem, aStart) - bEnd;

        int result = Math.abs(first) < Math.abs(second) ? first : second;
        return first > second ? result : -result;
    }

    private static boolean isEmpty(CoordinateSystem coordinateSystem, int start, int end) {
        return length(coordinateSystem, start, end) == 0;
    }

    private static int openStart(CoordinateSystem coordinateSystem, int start) {
        return coordinateSystem.startBound() == Bound.OPEN ? start : start - 1;
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
        if (start < 0) {
            throw new InvalidCoordinatesException("Cannot create start coordinate `" + start + "` with negative value");
        }
        if (end < 0) {
            throw new InvalidCoordinatesException("Cannot create end coordinate `" + end + "` with negative value");
        }
        switch (coordinateSystem) {
            case ONE_BASED:
                if (start > end + 1) {
                    // region [2,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("One-based coordinates " + start + '-' + end + " must have a start position at most one place past the end position");
                }
                break;
            case ZERO_BASED:
                if (start > end) {
                    // region (1,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("Zero-based coordinates " + start + '-' + end + " must have a start position before the end position");
                }
                break;
        }
    }

    /**
     * Checks whether a given set of coordinates is valid. This checks that the coordinates do not overflow the bounds
     * of the {@link Contig} and that the coordinates are provided in the correct orientation, i.e. indicate an empty or
     * positive interval where the end is generally 'downstream' or numerically greater than the start. Exceptions here
     * are empty intervals where:
     * <p>
     * fully-closed,  end = start - 1;
     * <p>
     * half-open,     end = start;
     * <p>
     * Invalid coordinates will result in an unrecoverable exception being thrown.
     *
     * @param coordinates      the given coordinates
     * @param contig           on which the coordinates lie.
     */
    public static void validateOnContig(Contig contig, Coordinates coordinates) {
        Objects.requireNonNull(contig);
        Objects.requireNonNull(coordinates);
        int start = coordinates.start();
        int end = coordinates.end();
        switch (coordinates.coordinateSystem()) {
            case ONE_BASED:
                if (start < 1 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("One-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 1 + ',' + contig.length() + ']');
                }
                break;
            case ZERO_BASED:
                if (start < 0 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("Zero-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 0 + ',' + contig.length() + ')');
                }
                break;
        }
    }

    /**
     * Checks whether a given set of coordinates is valid. This checks that the coordinates do not overflow the bounds
     * of the {@link Contig} and that the coordinates are provided in the correct orientation, i.e. indicate an empty or
     * positive interval where the end is generally 'downstream' or numerically greater than the start. Exceptions here
     * are empty intervals where:
     * <p>
     * fully-closed,  end = start - 1;
     * <p>
     * half-open,     end = start;
     * <p>
     * Invalid coordinates will result in an unrecoverable exception being thrown.
     *
     * @param coordinateSystem for the given coordinates
     * @param contig           on which the coordinates lie.
     * @param start            start of the interval
     * @param end              end of the interval
     */
    public static void validateCoordinates(CoordinateSystem coordinateSystem, Contig contig, int start, int end) {
        Objects.requireNonNull(coordinateSystem);
        Objects.requireNonNull(contig);
        switch (coordinateSystem) {
            case ONE_BASED:
                if (start < 1 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("One-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 1 + ',' + contig.length() + ']');
                }
                if (start > end + 1) {
                    // region [2,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("One-based coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position at most one place past the end position");
                }
                break;
            case ZERO_BASED:
                if (start < 0 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("Zero-based coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 0 + ',' + contig.length() + ')');
                }
                if (start > end) {
                    // region (1,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("Zero-based coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position before the end position");
                }
                break;
        }
    }
}
