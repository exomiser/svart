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
            "ONE_BASED,   100,  A,   100",
            "ONE_BASED,   100,  AT,  101",
            "ZERO_BASED,   100,  A,   101",
            "ZERO_BASED,   100,  AT,  102",
    })
    public void fromAllele(CoordinateSystem coordinateSystem, int start, String ref, int expectEnd) {
        Coordinates fromAllele = Coordinates.ofAllele(coordinateSystem, start, ref);
        Coordinates expected = Coordinates.of(coordinateSystem, start, expectEnd);
        assertEquals(expected, fromAllele);
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 1",
            "ZERO_BASED,    0",
    })
    public void breakend(CoordinateSystem coordinateSystem, int start) {
        Coordinates breakend = Coordinates.ofBreakend(coordinateSystem, start, ConfidenceInterval.precise());
        assertThat(breakend.length(), equalTo(0));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 1, 0,  0",
            "ONE_BASED, 1, 1,  1",
            "ONE_BASED, 1, 2,  2",

            "ZERO_BASED,    0, 0,  0",
            "ZERO_BASED,    0, 1,  1",
            "ZERO_BASED,    0, 2,  2",
    })
    public void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(Coordinates.length(coordinateSystem, start, end), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // given a coordinate on a contig of length 5
            "ONE_BASED, 1,  5",
            "ONE_BASED, 2,  4",
            "ONE_BASED, 3,  3",
            "ONE_BASED, 4,  2",
            "ONE_BASED, 5,  1",

            "ZERO_BASED,    0,  5",
            "ZERO_BASED,    1,  4",
            "ZERO_BASED,    2,  3",
            "ZERO_BASED,    3,  2",
            "ZERO_BASED,    4,  1",
            "ZERO_BASED,    5,  0",
    })
    public void invert(CoordinateSystem coordinateSystem, int pos, int expected) {
        Contig contig = TestContig.of(1, 5);
        assertThat(Coordinates.invertPosition(coordinateSystem, contig, pos), equalTo(expected));
    }

    @Nested
    public class Overlap {
        @ParameterizedTest
        @CsvSource({
                "ONE_BASED, 1, 1,  ONE_BASED, 1, 1,    true",
                "ONE_BASED, 1, 1,  ZERO_BASED,    0, 1,    true",
                "ONE_BASED, 1, 5,  ONE_BASED, 5, 7,    true",
                "ONE_BASED, 5, 5,  ONE_BASED, 5, 7,    true",
                "ONE_BASED, 1, 5,  ONE_BASED, 6, 8,    false",
                "ZERO_BASED,    1, 5,  ZERO_BASED,    1, 5,    true",
                "ZERO_BASED,    1, 5,  ZERO_BASED,    4, 8,    true",
                "ZERO_BASED,    1, 5,  ZERO_BASED,    5, 8,    false",
                "ZERO_BASED,    0, 1,  ONE_BASED, 1, 1,    true",
        })
        public void overlap(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty regions
                "ONE_BASED, 1, 0,  ONE_BASED, 1, 0,    true",
                "ONE_BASED, 1, 0,  ZERO_BASED,    0, 0,    true",

                "ZERO_BASED,    0, 0,  ONE_BASED, 1, 0,    true",
                "ZERO_BASED,    0, 0,  ZERO_BASED,    0, 0,    true",
        })
        public void overlap_emptyRegions(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty intervals on immediate boundaries
                "ONE_BASED,  1, 0,  ONE_BASED, 1, 1,    true",
                "ONE_BASED,  2, 1,  ONE_BASED, 1, 1,    true",
                "ONE_BASED,  1, 0,  ZERO_BASED,    0, 1,    true",
                "ONE_BASED,  2, 1,  ZERO_BASED,    0, 1,    true",

                "ZERO_BASED,     0, 0,  ONE_BASED, 1, 1,    true",
                "ZERO_BASED,     1, 1,  ONE_BASED, 1, 1,    true",
                "ZERO_BASED,     0, 0,  ZERO_BASED,    0, 1,    true",
                "ZERO_BASED,     1, 1,  ZERO_BASED,    0, 1,    true",
        })
        public void overlap_includesEmptyIntervalsOnBoundaries(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty
                "ZERO_BASED,     0, 0,  ZERO_BASED,   0, 1,    true",
                "ZERO_BASED,     0, 1,  ZERO_BASED,   0, 0,    true",

                // full
                "ZERO_BASED,     0, 5,  ZERO_BASED,   3, 8,    true",
                "ZERO_BASED,     3, 8,  ZERO_BASED,   0, 5,    true",
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
                "ONE_BASED, 1,  0",
                "ONE_BASED, 1,  1",
                "ONE_BASED, 1,  5",

                "ZERO_BASED,    0,  0",
                "ZERO_BASED,    0,  1",
                "ZERO_BASED,    0,  5",
        })
        public void validCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            assertDoesNotThrow(() -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
        }

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "ONE_BASED, 5,  6",
                "ONE_BASED, 6,  6",

                "ZERO_BASED,    5,  6",
                "ZERO_BASED,    6,  6",
        })
        public void coordinateOutOfBounds(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            Exception exception = assertThrows(CoordinatesOutOfBoundsException.class, () -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
            assertThat(exception.getMessage(), containsString("coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds"));
        }

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "ONE_BASED, 1,  -1",
                "ONE_BASED, 2,  0",

                "ZERO_BASED,    0,  -1",
                "ZERO_BASED,    2,  1",
        })
        public void invalidCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            Contig contig = TestContig.of(1, 5);
            Exception exception = assertThrows(InvalidCoordinatesException.class, () -> Coordinates.validateCoordinates(coordinateSystem, contig, start, end));
            assertThat(exception.getMessage(), containsString("coordinates " + contig.name() + ':' + start + '-' + end + " must have a start position"));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "ONE_BASED, -1",
        "ZERO_BASED,     0",
    })
    public void endDelta(CoordinateSystem coordinateSystem, int expected) {
        assertThat(Coordinates.endDelta(coordinateSystem), equalTo(expected));
    }

    @Nested
    public class DistanceTo {

        @ParameterizedTest
        @CsvSource({
                // overlapping regions, thus distance is 0
                "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    0",
                "ONE_BASED, 1, 2,   ZERO_BASED,     1, 3,    0",

                "ZERO_BASED,    0, 2,   ONE_BASED,  2, 3,    0",
                "ZERO_BASED,    0, 2,   ZERO_BASED,     1, 3,    0",
        })
        public void overlapping(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // adjacent, thus distance is 0
                "ONE_BASED, 1, 2,   ONE_BASED,    3, 4,    0",
                "ONE_BASED, 1, 2,   ZERO_BASED,       2, 4,    0",

                "ZERO_BASED,    0, 2,   ONE_BASED,    3, 4,    0",
                "ZERO_BASED,    0, 2,   ZERO_BASED,       2, 4,    0",
        })
        public void adjacent(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // all possible combinations of coordinate systems
                "ONE_BASED, 1, 2,   ONE_BASED,    5, 6,    2",
                "ONE_BASED, 1, 2,   ZERO_BASED,       4, 6,    2",

                "ZERO_BASED,    0, 2,   ONE_BASED,    5, 6,    2",
                "ZERO_BASED,    0, 2,   ZERO_BASED,       4, 6,    2",
        })
        public void distanceIsTwo(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }
    }

    @ParameterizedTest
    @CsvSource({
            // no overlap
            "ONE_BASED, 1, 2,   ONE_BASED,  8, 9,    0",
            "ONE_BASED, 1, 2,   ZERO_BASED,     8, 9,    0",

            // partial overlap, transitive
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 2, 3,   ONE_BASED,  1, 2,    1",
            "ONE_BASED, 1, 3,   ONE_BASED,  2, 4,    2",
            "ONE_BASED, 2, 4,   ONE_BASED,  1, 3,    2",

            // complete overlap
            "ONE_BASED, 1, 5,   ONE_BASED,  1, 5,    5",
            "ONE_BASED, 1, 5,   ZERO_BASED,     0, 5,    5",

            // multiple systems
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 1, 2,   ZERO_BASED,     1, 3,    1",

            "ZERO_BASED,    0, 2,   ONE_BASED,  2, 3,    1",
            "ZERO_BASED,    0, 3,   ONE_BASED,  2, 3,    2",
            "ZERO_BASED,    0, 2,   ZERO_BASED,     1, 3,    1",
    })
    public void overlapLength(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
        assertThat(Coordinates.overlapLength(x, xStart, xEnd, y, yStart, yEnd), is(expected));
    }

}
