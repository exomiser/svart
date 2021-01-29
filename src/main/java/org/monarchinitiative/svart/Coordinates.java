package org.monarchinitiative.svart;


import java.util.Objects;

import static org.monarchinitiative.svart.CoordinateSystem.LEFT_OPEN;

/**
 * Class providing methods for calculations involving genomic coordinates, where a coordinate is a position in a
 * {@link CoordinateSystem}. This class will handle the conversion of coordinates between coordinate systems such that
 * the user need only provide the coordinates in their original system. For example, it is safe to compare a pair of
 * coordinates for two intervals with different {@link CoordinateSystem}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class Coordinates {

    private Coordinates() {
        // uninstantiable
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
        return closedEnd(coordinateSystem, end) - openStart(coordinateSystem, start);
    }

    /**
     * Inverts the coordinate on the given contig. To be used when transforming coordinates from one strand to the
     * opposite strand. For example if the input coordinate indicates the first base on a contig, the output will return
     * the coordinate of the last base of a contig. This operation is symmetrical and inputting the output of one
     * operation will return the original input. For example given a contig of length 5 and a fully-closed ('1-based')
     * coordinate of 1 (i.e. the first base): invert(1) -> 5, invert(5) -> 1.
     *
     * @param coordinateSystem for the position
     * @param pos              position on the {@link Contig} in the given {@link CoordinateSystem}
     * @param contig           on which the coordinate lies.
     * @return the inverted coordinate on the {@link Contig}
     */
    public static int invertPosition(CoordinateSystem coordinateSystem, int pos, Contig contig) {
        return contig.length() + lengthDelta(coordinateSystem) - pos;
    }

    private static int lengthDelta(CoordinateSystem coordinateSystem) {
        return coordinateSystem == LEFT_OPEN ? 0 : LEFT_OPEN.startDelta(coordinateSystem) + LEFT_OPEN.endDelta(coordinateSystem);
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
        return openStart(aSystem, aStart) < closedEnd(bSystem, bEnd) && openStart(bSystem, bStart) < closedEnd(aSystem, aEnd);
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
        return openStart(aSystem, aStart) <= openStart(bSystem, bStart) && closedEnd(bSystem, bEnd) <= closedEnd(aSystem, aEnd);
    }

    /**
     * Returns the number of bases present between the intervals <code>a</code> and <code>b</code>. The distance is zero
     * if the <code>a</code> and <code>b</code> are adjacent or if they overlap. The distance is positive if <code>a</code>
     * is downstream of <code>b</code> and negative if <code>a</code> is located downstream from <code>b</code>.
     *
     * @param aSystem {@link CoordinateSystem} for interval described by positions aStart and aEnd
     * @param aStart  start coordinate of interval a
     * @param aEnd    end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart  start coordinate of interval b
     * @param bEnd    end coordinate of interval b
     * @return distance from interval <code>a</code> to interval <code>b</code>
     */
    public static int distanceTo(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        if (overlap(aSystem, aStart, aEnd, bSystem, bStart, bEnd)) return 0;

        int first = openStart(bSystem, bStart) - closedEnd(aSystem, aEnd) ;
        int second = openStart(aSystem, aStart) - closedEnd(bSystem, bEnd);

        int result = Math.abs(first) < Math.abs(second) ? first : second;
        return first > second ? result : -result;
    }

    private static boolean isEmpty(CoordinateSystem coordinateSystem, int start, int end) {
        return length(coordinateSystem, start, end) == 0;
    }

    private static int openStart(CoordinateSystem coordinateSystem, int start) {
        return coordinateSystem.startBound() == Bound.OPEN ? start : start - 1;
    }

    private static int closedEnd(CoordinateSystem coordinateSystem, int end) {
        return coordinateSystem.endBound() == Bound.CLOSED ? end : end - 1;
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
     * fully-open,    end = start + 1.
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
            case FULLY_CLOSED:
                if (start < 1 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("Fully-closed coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 1 + ',' + contig.length() + ']');
                }
                if (start > end + 1) {
                    // region [2,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("Fully-closed coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position at most one place past the end position");
                }
                break;
            case LEFT_OPEN:
                if (start < 0 || end > contig.length()) {
                    throw new CoordinatesOutOfBoundsException("Left-open coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds (" + 0 + ',' + contig.length() + ']');
                }
                if (start > end) {
                    // region (1,1] is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("Left-open coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position before the end position");
                }
                break;
            case RIGHT_OPEN:
                if (start < 1 || end > contig.length() + 1) {
                    throw new CoordinatesOutOfBoundsException("Right-open coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds [" + 1 + ',' + (contig.length() + 1) + ']');
                }
                if (start > end) {
                    // same check as in ZERO_BASED, [2,2) is an empty region, equivalent to (1,2)
                    throw new InvalidCoordinatesException("Right-open coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position before the end position");
                }
                break;
            case FULLY_OPEN:
                if (start < 0 || end > contig.length() + 1) {
                    throw new CoordinatesOutOfBoundsException("Fully-open coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds (" + 0 + ',' + (contig.length() + 1) + ')');
                }
                if (start >= end) {
                    // region in FULLY_OPEN coordinates - empty (1, 2), (1, 3) == [2, 2] == (1, 2] == [2, 3)
                    throw new InvalidCoordinatesException("Fully-open coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position at least one place before end position");
                }
                break;
        }
    }
}
