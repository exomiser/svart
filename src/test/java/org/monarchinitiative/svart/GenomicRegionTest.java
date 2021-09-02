package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = TestContig.of(1, 5);

    @Test
    public void oneBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(1));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance, equalTo(GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(1))));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.FULLY_CLOSED, Position.of(5), Position.of(5))));
        assertThat(instance.toOppositeStrand().toZeroBased(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.LEFT_OPEN, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(0), Position.of(1));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.LEFT_OPEN, Position.of(4), Position.of(5))));
    }

    @Test
    public void oneBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(2));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.FULLY_CLOSED, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(0), Position.of(2));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.LEFT_OPEN, Position.of(3), Position.of(5))));
    }

    @Test
    public void flipStrandAndChangeCoordinateSystem() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(0), Position.of(2));
        assertThat(instance.toOppositeStrand().toOneBased(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.FULLY_CLOSED, Position.of(4), Position.of(5))));
    }

    @ParameterizedTest
    @CsvSource({
            // region            pos
            // region before pos 2
            "FULLY_OPEN,   0, 2,   2,   false",
            "LEFT_OPEN,    0, 1,   2,   false",
            "RIGHT_OPEN,   1, 2,   2,   false",
            "FULLY_CLOSED, 1, 1,   2,   false",
            // region containing pos 2
            "FULLY_OPEN,   1, 3,   2,   true",
            "LEFT_OPEN,    1, 2,   2,   true",
            "RIGHT_OPEN,   2, 3,   2,   true",
            "FULLY_CLOSED, 2, 2,   2,   true",
            // region after pos 2
            "FULLY_OPEN,   2, 4,   2,   false",
            "LEFT_OPEN,    2, 3,   2,   false",
            "RIGHT_OPEN,   3, 4,   2,   false",
            "FULLY_CLOSED, 3, 3,   2,   false",
    })
    public void containsPosition(CoordinateSystem coordinateSystem, int start, int end,
                                 int position,
                                 boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end));
        assertThat(region.contains(Position.of(position)), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 1, 2,   true",
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 2, 3,   false",

            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 1, 2,   true",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 2, 3,   true",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 3, 4,   false",

            // --------------------------------------------

            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  2, 3,   true",
            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  3, 3,   true",
            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  3, 4,   true",

            //  LEFT_OPEN, POS:     0 1 2 3 4
            //  FULLY_CLOSED, POS:  1 2 3 4 5
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  2, 3,   true",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  3, 4,   true",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  4, 4,   true",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  4, 5,   true",

            // -----------------------------------------------
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            "POSITIVE, LEFT_OPEN, 1, 2,   NEGATIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 0, 5,   NEGATIVE, LEFT_OPEN, 0, 5,   true",
            "POSITIVE, LEFT_OPEN, 1, 5,   NEGATIVE, LEFT_OPEN, 1, 5,   true",
            "POSITIVE, LEFT_OPEN, 2, 5,   NEGATIVE, LEFT_OPEN, 2, 5,   true",
            "POSITIVE, LEFT_OPEN, 3, 5,   NEGATIVE, LEFT_OPEN, 3, 5,   false",
            "POSITIVE, LEFT_OPEN, 2, 3,   NEGATIVE, LEFT_OPEN, 2, 3,   true",
            "POSITIVE, LEFT_OPEN, 3, 4,   NEGATIVE, LEFT_OPEN, 3, 4,   false",
            "POSITIVE, LEFT_OPEN, 1, 3,   NEGATIVE, LEFT_OPEN, 2, 4,   true",
    })
    public void GenomigRegionOverlapsOther(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                              Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                              boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

        assertThat(region.overlapsWith(other), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 1, 2,   true",
            "POSITIVE, LEFT_OPEN, 1, 2,   POSITIVE, LEFT_OPEN, 2, 3,   false",

            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 1, 2,   true",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 2, 3,   true",
            "POSITIVE, LEFT_OPEN, 1, 3,   POSITIVE, LEFT_OPEN, 3, 4,   false",

            // --------------------------------------------

            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  2, 3,   false",
            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  3, 3,   true",
            "POSITIVE, LEFT_OPEN, 2, 3,   POSITIVE, FULLY_CLOSED,  3, 4,   false",

            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  2, 3,   false",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  3, 4,   true",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  4, 4,   true",
            "POSITIVE, LEFT_OPEN, 2, 4,   POSITIVE, FULLY_CLOSED,  4, 5,   false",

            // -----------------------------------------------
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            "POSITIVE, LEFT_OPEN, 1, 2,   NEGATIVE, LEFT_OPEN, 0, 1,   false",
            "POSITIVE, LEFT_OPEN, 0, 5,   NEGATIVE, LEFT_OPEN, 0, 5,   true",
            "POSITIVE, LEFT_OPEN, 1, 5,   NEGATIVE, LEFT_OPEN, 1, 5,   false",
            "POSITIVE, LEFT_OPEN, 2, 5,   NEGATIVE, LEFT_OPEN, 2, 5,   false",
            "POSITIVE, LEFT_OPEN, 2, 3,   NEGATIVE, LEFT_OPEN, 2, 3,   true",
            "POSITIVE, LEFT_OPEN, 3, 4,   NEGATIVE, LEFT_OPEN, 3, 4,   false",
            "POSITIVE, LEFT_OPEN, 1, 3,   NEGATIVE, LEFT_OPEN, 2, 4,   true",
    })
    public void zeroBasedRegionContainsRegion(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                              Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                               boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

        assertThat(region.contains(other), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "FULLY_CLOSED, 1, 1,   LEFT_OPEN, 0, 1,   true",
            "FULLY_CLOSED, 1, 1,   LEFT_OPEN, 1, 2,   false",

            "FULLY_CLOSED, 2, 3,   LEFT_OPEN, 0, 1,   false",
            "FULLY_CLOSED, 2, 3,   LEFT_OPEN, 1, 2,   true",
            "FULLY_CLOSED, 2, 3,   LEFT_OPEN, 2, 3,   true",
            "FULLY_CLOSED, 2, 3,   LEFT_OPEN, 3, 4,   false",

            "FULLY_CLOSED, 2, 4,   LEFT_OPEN, 0, 1,   false",
            "FULLY_CLOSED, 2, 4,   LEFT_OPEN, 1, 2,   true",
            "FULLY_CLOSED, 2, 4,   LEFT_OPEN, 2, 3,   true",
            "FULLY_CLOSED, 2, 4,   LEFT_OPEN, 3, 4,   true",
            "FULLY_CLOSED, 2, 4,   LEFT_OPEN, 4, 5,   false",
            // --------------------------------------------
            "FULLY_CLOSED, 2, 2,   FULLY_CLOSED,  1, 1,   false",
            "FULLY_CLOSED, 2, 2,   FULLY_CLOSED,  2, 2,   true",
            "FULLY_CLOSED, 2, 2,   FULLY_CLOSED,  3, 3,   false",

            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  1, 2,   false",
            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  2, 2,   true",
            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  2, 3,   true",
            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  3, 3,   true",
            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  3, 4,   false",

            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  1, 3,   false",
            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  2, 3,   true",
            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  2, 4,   true",
            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  3, 4,   true",
            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  3, 5,   false",
    })
    public void oneBasedRegionContainsRegion(CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                               CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                               boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, Strand.POSITIVE, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

        assertThat(region.contains(other), equalTo(expected));
    }

    @Test
    public void containsRegion_otherContig() {
        GenomicRegion oneToThree = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(3));
        Contig ctg2 = TestContig.of(2, 200);
        GenomicRegion other = GenomicRegion.of(ctg2, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(2), Position.of(3));
        assertThat(oneToThree.contains(other), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, 4, 5,   POSITIVE, 1,  2,  -1",
            "POSITIVE, 4, 5,   NEGATIVE, 9, 10,  -1",
            "POSITIVE, 4, 5,   POSITIVE, 2,  3,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 8,  9,   0",
            "POSITIVE, 4, 5,   POSITIVE, 3,  4,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 7,  8,   0",
            "POSITIVE, 4, 5,   POSITIVE, 6,  7,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 4,  5,   0",
            "POSITIVE, 4, 5,   POSITIVE, 7,  8,   1",
            "POSITIVE, 4, 5,   NEGATIVE, 3,  4,   1",
    })
    public void distanceTo(Strand xStrand, int xStart, int xEnd,
                           Strand yStrand, int yStart, int yEnd,
                           int expected) {
        Contig ctg1 = TestContig.of(1, 10);
        GenomicRegion x = GenomicRegion.of(ctg1, xStrand, CoordinateSystem.oneBased(), Position.of(xStart), Position.of(xEnd));
        GenomicRegion y = GenomicRegion.of(ctg1, yStrand, CoordinateSystem.oneBased(), Position.of(yStart), Position.of(yEnd));
        assertThat(x.distanceTo(y), equalTo(expected));
    }

    @Test
    public void distanceTo_otherContig() {
        GenomicRegion x = GenomicRegion.of(TestContig.of(1, 10), Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(1), Position.of(2));
        GenomicRegion y = GenomicRegion.of(TestContig.of(2, 10), Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(1), Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> x.distanceTo(y));
        assertThat(e.getMessage(), equalTo("Cannot calculate distance between regions on different contigs: 1 <-> 2"));

        IllegalArgumentException f = assertThrows(IllegalArgumentException.class, () -> y.distanceTo(x));
        assertThat(f.getMessage(), equalTo("Cannot calculate distance between regions on different contigs: 2 <-> 1"));
    }

    @Test
    public void withStrand_emptyRegion() {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_OPEN, Position.of(0), Position.of(1));

        GenomicRegion negative = region.withStrand(Strand.NEGATIVE);
        assertThat(negative.startPosition(), equalTo(Position.of(5)));
        assertThat(negative.endPosition(), equalTo(Position.of(6)));
        assertThat(negative.coordinateSystem(), equalTo(CoordinateSystem.FULLY_OPEN));
    }

    @ParameterizedTest
    @CsvSource({
            // we test 2-bp-long region that spans the bases 3-4 of the imaginary contig with length 5
            // source     start      end       target   start  end
            "POSITIVE,    FULLY_OPEN, 2,  5,   NEGATIVE,    1, 4",
            "POSITIVE,    LEFT_OPEN, 2,  4,   NEGATIVE,    1, 3",
            "POSITIVE,    RIGHT_OPEN, 3,  5,   NEGATIVE,    2, 4",
            "POSITIVE,    FULLY_CLOSED,  3,  4,   NEGATIVE,    2, 3",
            "NEGATIVE,    FULLY_OPEN, 1,  4,   POSITIVE,    2, 5",
            "NEGATIVE,    LEFT_OPEN, 1,  3,   POSITIVE,    2, 4",
            "NEGATIVE,    RIGHT_OPEN, 2,  4,   POSITIVE,    3, 5",
            "NEGATIVE,    FULLY_CLOSED,  2,  3,   POSITIVE,    3, 4",

            // converting to the same strand preserves the coordinates
            "POSITIVE,    FULLY_OPEN, 2,  5,   POSITIVE,    2, 5",
            "POSITIVE,    LEFT_OPEN, 2,  4,   POSITIVE,    2, 4",
            "POSITIVE,    RIGHT_OPEN, 3,  5,   POSITIVE,    3, 5",
            "POSITIVE,    FULLY_CLOSED,  3,  4,   POSITIVE,    3, 4",
            "NEGATIVE,    FULLY_OPEN, 1,  4,   NEGATIVE,    1, 4",
            "NEGATIVE,    LEFT_OPEN, 1,  3,   NEGATIVE,    1, 3",
            "NEGATIVE,    RIGHT_OPEN, 2,  4,   NEGATIVE,    2, 4",
            "NEGATIVE,    FULLY_CLOSED,  2,  3,   NEGATIVE,    2, 3",
    })
    public void withStrand_strandConversions(Strand source, CoordinateSystem coordinateSystem, int start, int end,
                                             Strand target,
                                             int exptStart, int exptEnd) {
        GenomicRegion initial = GenomicRegion.of(chr1, source, coordinateSystem, Position.of(start), Position.of(end));

        GenomicRegion actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(target));
        assertThat(actual.start(), equalTo(exptStart));
        assertThat(actual.end(), equalTo(exptEnd));
        assertThat(actual.coordinateSystem(), equalTo(initial.coordinateSystem()));
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT_OPEN, 2, 3,   0,   2, 3",
            "FULLY_CLOSED,  2, 3,   0,   2, 3",
            "LEFT_OPEN, 2, 3,   1,   1, 4",
            "FULLY_CLOSED,  2, 3,   1,   1, 4",
            "LEFT_OPEN, 2, 3,   2,   0, 5",
    })
    public void withPadding_singlePadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(padding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT_OPEN, 2, 3,   0, 0,   2, 3",
            "FULLY_CLOSED,  2, 3,   0, 0,   2, 3",
            "LEFT_OPEN, 2, 3,   1, 2,   1, 5",
            "FULLY_CLOSED,  2, 3,   1, 2,   1, 5",
            "LEFT_OPEN, 2, 3,   2, 0,   0, 3",
            "FULLY_CLOSED,  2, 3,   1, 0,   1, 3",
    })
    public void withPadding_upDownPadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int upPadding, int downPadding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(upPadding, downPadding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED,   2, 3,    CLOSED,   2",
            "FULLY_CLOSED,   2, 3,      OPEN,   1",
            "LEFT_OPEN,  2, 3,    CLOSED,   3",
            "LEFT_OPEN,  2, 3,      OPEN,   2",
            "RIGHT_OPEN,  2, 3,    CLOSED,   2",
            "RIGHT_OPEN,  2, 3,      OPEN,   1",
            "FULLY_OPEN,  2, 3,    CLOSED,   3",
            "FULLY_OPEN,  2, 3,      OPEN,   2",
    })
    public void startPositionWithCoordinateSystem(CoordinateSystem coordinateSystem, int startPos, int endPos, Bound targetBound, int expectedStart) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(startPos), Position.of(endPos));

        CoordinateSystem targetCoordinateSystem = targetBound == Bound.OPEN ? CoordinateSystem.FULLY_OPEN : CoordinateSystem.FULLY_CLOSED;
        Position normalisedStartPosition = region.startPositionWithCoordinateSystem(targetCoordinateSystem);
        assertThat(normalisedStartPosition, equalTo(Position.of(expectedStart)));

        if (targetBound.equals(coordinateSystem.startBound())) {
            assertThat(normalisedStartPosition, equalTo(region.startPosition()));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED,   2, 3,   CLOSED,   3",
            "FULLY_CLOSED,   2, 3,     OPEN,   4",
            "LEFT_OPEN,  2, 3,   CLOSED,   3",
            "LEFT_OPEN,  2, 3,     OPEN,   4",
            "RIGHT_OPEN,  2, 3,   CLOSED,   2",
            "RIGHT_OPEN,  2, 3,     OPEN,   3",
            "FULLY_OPEN,  2, 3,   CLOSED,   2",
            "FULLY_OPEN,  2, 3,     OPEN,   3",
    })
    public void endPositionWithCoordinateSystem(CoordinateSystem coordinateSystem, int startPos, int endPos,
                                     Bound targetBound, int expectedEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(startPos), Position.of(endPos));

        CoordinateSystem targetCoordinateSystem = targetBound == Bound.OPEN ? CoordinateSystem.FULLY_OPEN : CoordinateSystem.FULLY_CLOSED;
        Position normalisedEndPosition = region.endPositionWithCoordinateSystem(targetCoordinateSystem);
        assertThat(normalisedEndPosition, equalTo(Position.of(expectedEnd)));

        if (targetBound.equals(coordinateSystem.endBound())) {
            assertThat(normalisedEndPosition, equalTo(region.endPosition()));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, FULLY_CLOSED,   2, 3,    POSITIVE, FULLY_CLOSED, 2",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, FULLY_CLOSED, 3",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, LEFT_OPEN,    2",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, RIGHT_OPEN,   3",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, FULLY_OPEN,   2",

            "POSITIVE, FULLY_OPEN,     1, 4,    POSITIVE, FULLY_OPEN,   1",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, FULLY_CLOSED, 3",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, LEFT_OPEN,    2",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, RIGHT_OPEN,   3",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, FULLY_OPEN,   2",
    })
    public void startOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem, int startPos, int endPos, Strand targetStrand, CoordinateSystem targetCoordinateSystem, int expectedStart) {
        GenomicRegion region = GenomicRegion.of(chr1, strand, coordinateSystem, Position.of(startPos), Position.of(endPos));
        assertThat(region.startOnStrandWithCoordinateSystem(targetStrand, targetCoordinateSystem), equalTo(expectedStart));
        assertThat(region.withStrand(targetStrand).withCoordinateSystem(targetCoordinateSystem).start(), equalTo(expectedStart));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, FULLY_CLOSED,   2, 3,    POSITIVE, FULLY_CLOSED, 3",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, FULLY_CLOSED, 4",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, LEFT_OPEN,    4",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, RIGHT_OPEN,   5",
            "POSITIVE, FULLY_CLOSED,   2, 3,    NEGATIVE, FULLY_OPEN,   5",

            "POSITIVE, FULLY_OPEN,     1, 4,    POSITIVE, FULLY_OPEN,   4",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, FULLY_CLOSED, 4",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, LEFT_OPEN,    4",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, RIGHT_OPEN,   5",
            "POSITIVE, FULLY_OPEN,     1, 4,    NEGATIVE, FULLY_OPEN,   5",
    })
    public void endOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem, int startPos, int endPos, Strand targetStrand, CoordinateSystem targetCoordinateSystem, int expectedEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, strand, coordinateSystem, Position.of(startPos), Position.of(endPos));
        assertThat(region.endOnStrandWithCoordinateSystem(targetStrand, targetCoordinateSystem), equalTo(expectedEnd));
        assertThat(region.withStrand(targetStrand).withCoordinateSystem(targetCoordinateSystem).end(), equalTo(expectedEnd));
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_OPEN, 1, 4,    LEFT_OPEN, 1, 3",
            "FULLY_OPEN, 1, 4,     FULLY_CLOSED, 2, 3",
            "FULLY_OPEN, 1, 4,    FULLY_OPEN, 1, 4",
            "FULLY_OPEN, 1, 4,    RIGHT_OPEN, 2, 4",

            "RIGHT_OPEN, 2, 4,    LEFT_OPEN, 1, 3",
            "RIGHT_OPEN, 2, 4,     FULLY_CLOSED, 2, 3",
            "RIGHT_OPEN, 2, 4,    FULLY_OPEN, 1, 4",
            "RIGHT_OPEN, 2, 4,    RIGHT_OPEN, 2, 4",

            "LEFT_OPEN, 1, 3,    LEFT_OPEN, 1, 3",
            "LEFT_OPEN, 1, 3,     FULLY_CLOSED, 2, 3",
            "LEFT_OPEN, 1, 3,    FULLY_OPEN, 1, 4",
            "LEFT_OPEN, 1, 3,    RIGHT_OPEN, 2, 4",

            " FULLY_CLOSED, 2, 3,    LEFT_OPEN, 1, 3",
            " FULLY_CLOSED, 2, 3,     FULLY_CLOSED, 2, 3",
            " FULLY_CLOSED, 2, 3,    FULLY_OPEN, 1, 4",
            " FULLY_CLOSED, 2, 3,    RIGHT_OPEN, 2, 4",
    })
    public void withCoordinateSystem(CoordinateSystem source, int start, int end,
                                     CoordinateSystem target, int targetStart, int targetEnd) {

        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, source, start, end);

        GenomicRegion other = region.withCoordinateSystem(target);
        assertThat(other.start(), equalTo(targetStart));
        assertThat(other.coordinateSystem().startBound(), equalTo(target.startBound()));
        assertThat(other.end(), equalTo(targetEnd));
        assertThat(other.coordinateSystem().endBound(), equalTo(target.endBound()));
    }

    @ParameterizedTest
    @CsvSource({
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            // this                       other                        expected
            "POSITIVE, LEFT_OPEN, 1, 2,   NEGATIVE, LEFT_OPEN, 0, 1,   0",
            "POSITIVE, LEFT_OPEN, 0, 5,   NEGATIVE, LEFT_OPEN, 0, 5,   5",
            "POSITIVE, LEFT_OPEN, 1, 5,   NEGATIVE, LEFT_OPEN, 0, 5,   4",
            "POSITIVE, LEFT_OPEN, 1, 5,   NEGATIVE, LEFT_OPEN, 1, 5,   3",
            "POSITIVE, LEFT_OPEN, 1, 3,   NEGATIVE, LEFT_OPEN, 2, 4,   2",
            "POSITIVE, LEFT_OPEN, 2, 5,   NEGATIVE, LEFT_OPEN, 2, 5,   1",
            "POSITIVE, LEFT_OPEN, 2, 3,   NEGATIVE, LEFT_OPEN, 2, 3,   1",
            "POSITIVE, LEFT_OPEN, 3, 4,   NEGATIVE, LEFT_OPEN, 3, 4,   0",
    })
    public void overlapLength(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                              Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                              int expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

        assertThat(region.overlapLength(other), equalTo(expected));
    }

}