package org.monarchinitiative.svart;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class CoordinatesTest {

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED,   100,  A,   100",
            "FULLY_CLOSED,   100,  AT,  101",
            "LEFT_OPEN,   100,  A,   101",
            "LEFT_OPEN,   100,  AT,  102",
    })
    public void fromAllele(CoordinateSystem coordinateSystem, int start, String ref, int expectEnd) {
        Coordinates fromAllele = Coordinates.ofAllele(coordinateSystem, start, ref);
        Coordinates expected = Coordinates.of(coordinateSystem, start, expectEnd);
        assertEquals(expected, fromAllele);
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 1",
            "LEFT_OPEN,    0",
    })
    public void breakend(CoordinateSystem coordinateSystem, int start) {
        Coordinates breakend = Coordinates.ofBreakend(coordinateSystem, start, ConfidenceInterval.precise());
        assertThat(breakend.length(), equalTo(0));
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 1, 0,  0",
            "FULLY_CLOSED, 1, 1,  1",
            "FULLY_CLOSED, 1, 2,  2",

            "LEFT_OPEN,    0, 0,  0",
            "LEFT_OPEN,    0, 1,  1",
            "LEFT_OPEN,    0, 2,  2",
    })
    public void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(Coordinates.length(coordinateSystem, start, end), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // given a coordinate on a contig of length 5
            "FULLY_CLOSED, 1,  5",
            "FULLY_CLOSED, 2,  4",
            "FULLY_CLOSED, 3,  3",
            "FULLY_CLOSED, 4,  2",
            "FULLY_CLOSED, 5,  1",

            "LEFT_OPEN,    0,  5",
            "LEFT_OPEN,    1,  4",
            "LEFT_OPEN,    2,  3",
            "LEFT_OPEN,    3,  2",
            "LEFT_OPEN,    4,  1",
            "LEFT_OPEN,    5,  0",
    })
    public void invert(CoordinateSystem coordinateSystem, int pos, int expected) {
        Contig contig = TestContig.of(1, 5);
        assertThat(Coordinates.invertPosition(coordinateSystem, contig, pos), equalTo(expected));
    }

    @Nested
    public class Overlap {
        @ParameterizedTest
        @CsvSource({
                "FULLY_CLOSED, 1, 1,  FULLY_CLOSED, 1, 1,    true",
                "FULLY_CLOSED, 1, 1,  LEFT_OPEN,    0, 1,    true",
                "FULLY_CLOSED, 1, 5,  FULLY_CLOSED, 5, 7,    true",
                "FULLY_CLOSED, 5, 5,  FULLY_CLOSED, 5, 7,    true",
                "FULLY_CLOSED, 1, 5,  FULLY_CLOSED, 6, 8,    false",
                "LEFT_OPEN,    1, 5,  LEFT_OPEN,    1, 5,    true",
                "LEFT_OPEN,    1, 5,  LEFT_OPEN,    4, 8,    true",
                "LEFT_OPEN,    1, 5,  LEFT_OPEN,    5, 8,    false",
                "LEFT_OPEN,    0, 1,  FULLY_CLOSED, 1, 1,    true",
        })
        public void overlap(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty regions
                "FULLY_CLOSED, 1, 0,  FULLY_CLOSED, 1, 0,    true",
                "FULLY_CLOSED, 1, 0,  LEFT_OPEN,    0, 0,    true",

                "LEFT_OPEN,    0, 0,  FULLY_CLOSED, 1, 0,    true",
                "LEFT_OPEN,    0, 0,  LEFT_OPEN,    0, 0,    true",
        })
        public void overlap_emptyRegions(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty intervals on immediate boundaries
                "FULLY_CLOSED,  1, 0,  FULLY_CLOSED, 1, 1,    true",
                "FULLY_CLOSED,  2, 1,  FULLY_CLOSED, 1, 1,    true",
                "FULLY_CLOSED,  1, 0,  LEFT_OPEN,    0, 1,    true",
                "FULLY_CLOSED,  2, 1,  LEFT_OPEN,    0, 1,    true",

                "LEFT_OPEN,     0, 0,  FULLY_CLOSED, 1, 1,    true",
                "LEFT_OPEN,     1, 1,  FULLY_CLOSED, 1, 1,    true",
                "LEFT_OPEN,     0, 0,  LEFT_OPEN,    0, 1,    true",
                "LEFT_OPEN,     1, 1,  LEFT_OPEN,    0, 1,    true",
        })
        public void overlap_includesEmptyIntervalsOnBoundaries(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty
                "LEFT_OPEN,     0, 0,  LEFT_OPEN,   0, 1,    true",
                "LEFT_OPEN,     0, 1,  LEFT_OPEN,   0, 0,    true",

                // full
                "LEFT_OPEN,     0, 5,  LEFT_OPEN,   3, 8,    true",
                "LEFT_OPEN,     3, 8,  LEFT_OPEN,   0, 5,    true",
        })
        public void overlap_isTransitive(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }
    }

    @Nested
    public class Validate {

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "FULLY_CLOSED, 1,  0",
                "FULLY_CLOSED, 1,  1",
                "FULLY_CLOSED, 1,  5",

                "LEFT_OPEN,    0,  0",
                "LEFT_OPEN,    0,  1",
                "LEFT_OPEN,    0,  5",
        })
        public void validCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            assertDoesNotThrow(() -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
        }

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "FULLY_CLOSED, 5,  6",
                "FULLY_CLOSED, 6,  6",

                "LEFT_OPEN,    5,  6",
                "LEFT_OPEN,    6,  6",
        })
        public void coordinateOutOfBounds(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            Exception exception = assertThrows(CoordinatesOutOfBoundsException.class, () -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
            assertThat(exception.getMessage(), containsString("coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds"));
        }

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "FULLY_CLOSED, 1,  -1",
                "FULLY_CLOSED, 2,  0",

                "LEFT_OPEN,    0,  -1",
                "LEFT_OPEN,    2,  1",
        })
        public void invalidCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            Exception exception = assertThrows(InvalidCoordinatesException.class, () -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
            assertThat(exception.getMessage(), containsString("coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position"));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "FULLY_CLOSED, -1",
        "LEFT_OPEN,     0",
    })
    public void endDelta(CoordinateSystem coordinateSystem, int expected) {
        assertThat(Coordinates.endDelta(coordinateSystem), equalTo(expected));
    }

    @Nested
    public class DistanceTo {

        @ParameterizedTest
        @CsvSource({
                // overlapping regions, thus distance is 0
                "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,  2, 3,    0",
                "FULLY_CLOSED, 1, 2,   LEFT_OPEN,     1, 3,    0",

                "LEFT_OPEN,    0, 2,   FULLY_CLOSED,  2, 3,    0",
                "LEFT_OPEN,    0, 2,   LEFT_OPEN,     1, 3,    0",
        })
        public void overlapping(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // adjacent, thus distance is 0
                "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,    3, 4,    0",
                "FULLY_CLOSED, 1, 2,   LEFT_OPEN,       2, 4,    0",

                "LEFT_OPEN,    0, 2,   FULLY_CLOSED,    3, 4,    0",
                "LEFT_OPEN,    0, 2,   LEFT_OPEN,       2, 4,    0",
        })
        public void adjacent(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // all possible combinations of coordinate systems
                "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,    5, 6,    2",
                "FULLY_CLOSED, 1, 2,   LEFT_OPEN,       4, 6,    2",

                "LEFT_OPEN,    0, 2,   FULLY_CLOSED,    5, 6,    2",
                "LEFT_OPEN,    0, 2,   LEFT_OPEN,       4, 6,    2",
        })
        public void distanceIsTwo(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }
    }

    @ParameterizedTest
    @CsvSource({
            // no overlap
            "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,  8, 9,    0",
            "FULLY_CLOSED, 1, 2,   LEFT_OPEN,     8, 9,    0",

            // partial overlap, transitive
            "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,  2, 3,    1",
            "FULLY_CLOSED, 2, 3,   FULLY_CLOSED,  1, 2,    1",
            "FULLY_CLOSED, 1, 3,   FULLY_CLOSED,  2, 4,    2",
            "FULLY_CLOSED, 2, 4,   FULLY_CLOSED,  1, 3,    2",

            // complete overlap
            "FULLY_CLOSED, 1, 5,   FULLY_CLOSED,  1, 5,    5",
            "FULLY_CLOSED, 1, 5,   LEFT_OPEN,     0, 5,    5",

            // multiple systems
            "FULLY_CLOSED, 1, 2,   FULLY_CLOSED,  2, 3,    1",
            "FULLY_CLOSED, 1, 2,   LEFT_OPEN,     1, 3,    1",

            "LEFT_OPEN,    0, 2,   FULLY_CLOSED,  2, 3,    1",
            "LEFT_OPEN,    0, 3,   FULLY_CLOSED,  2, 3,    2",
            "LEFT_OPEN,    0, 2,   LEFT_OPEN,     1, 3,    1",
    })
    public void overlapLength(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
        assertThat(Coordinates.overlapLength(x, xStart, xEnd, y, yStart, yEnd), is(expected));
    }

}
