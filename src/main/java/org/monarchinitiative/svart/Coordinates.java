package org.monarchinitiative.svart;


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
     * @param start start coordinate of the region
     * @param end end coordinate of the region
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
     * @param pos position on the {@link Contig} in the given {@link CoordinateSystem}
     * @param contig on which the coordinate lies.
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
     * @param aStart start coordinate of interval a
     * @param aEnd end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart start coordinate of interval b
     * @param bEnd end coordinate of interval b
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
     * @param aStart start coordinate of interval a
     * @param aEnd end coordinate of interval a
     * @param bSystem {@link CoordinateSystem} for interval described by positions bStart and bEnd
     * @param bStart start coordinate of interval b
     * @param bEnd end coordinate of interval b
     * @return true indicating interval a fully contains b or false if it does not.
     */
    public static boolean aContainsB(CoordinateSystem aSystem, int aStart, int aEnd, CoordinateSystem bSystem, int bStart, int bEnd) {
        return openStart(aSystem, aStart) <= openStart(bSystem, bStart) && closedEnd(bSystem, bEnd) <= closedEnd(aSystem, aEnd);
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
}
