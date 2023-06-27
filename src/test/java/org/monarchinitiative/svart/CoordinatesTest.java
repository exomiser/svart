package org.monarchinitiative.svart;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void emptyCoordinates() {
        assertThat(Coordinates.empty(), equalTo(Coordinates.zeroBased(0, 0)));
    }

    @Test
    void zeroBasedConstructorPrecise() {
        Coordinates instance = Coordinates.zeroBased(0, 10);
        assertThat(instance, equalTo(Coordinates.of(CoordinateSystem.zeroBased(), 0, ConfidenceInterval.precise(), 10, ConfidenceInterval.precise())));
        assertThat(instance.isPrecise(), is(true));
    }

    @Test
    void zeroBasedConstructorImprecise() {
        Coordinates instance = Coordinates.zeroBased(0, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise());
        assertThat(instance, equalTo(Coordinates.of(CoordinateSystem.zeroBased(), 0, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise())));
        assertThat(instance.isPrecise(), is(false));
    }

    @Test
    void oneBasedConstructorPrecise() {
        Coordinates instance = Coordinates.oneBased(1, 10);
        assertThat(instance, equalTo(Coordinates.of(CoordinateSystem.oneBased(), 1, ConfidenceInterval.precise(), 10, ConfidenceInterval.precise())));
    }

    @Test
    void oneBasedConstructorImprecise() {
        Coordinates instance = Coordinates.oneBased(1, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise());
        assertThat(instance, equalTo(Coordinates.of(CoordinateSystem.oneBased(), 1, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise())));
    }

    @Nested
    class Imprecise {

        private final Coordinates instance = Coordinates.zeroBased(0, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise());

        @Test
        void impreciseToPrecise() {
            assertThat(instance, equalTo(Coordinates.of(CoordinateSystem.zeroBased(), 0, ConfidenceInterval.of(0, 2), 10, ConfidenceInterval.precise())));
            assertThat(instance.isPrecise(), is(false));
            Coordinates asPrecise = instance.asPrecise();
            assertThat(asPrecise, equalTo(Coordinates.of(CoordinateSystem.zeroBased(), 0, 10)));
            assertThat(asPrecise.isPrecise(), is(true));
        }

        @Test
        void withCoordinateSystem() {
            assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
            assertThat(instance.withCoordinateSystem(CoordinateSystem.ZERO_BASED).coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
            assertThat(instance.withCoordinateSystem(CoordinateSystem.ONE_BASED).coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        }

        @Test
        void extend() {
            assertThat(instance.extend(0, 0), equalTo(instance));
            assertThat(instance.extend(-5, 10), equalTo(Coordinates.zeroBased(5, ConfidenceInterval.of(0, 2), 20, ConfidenceInterval.precise())));
        }
    }
    
    @Test
    void testStartTypes() {
        Coordinates oneBasedCoords = Coordinates.oneBased(1, 10);
        Coordinates zeroBasedCoords = Coordinates.zeroBased(0, 10);
        assertThat(oneBasedCoords.start(), not(equalTo(zeroBasedCoords.start())));
        assertThat(oneBasedCoords.startZeroBased(), equalTo(zeroBasedCoords.start()));
        assertThat(oneBasedCoords.start(), equalTo(zeroBasedCoords.startOneBased()));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   100,  A,   100",
            "ONE_BASED,   100,  AT,  101",
            "ZERO_BASED,  100,  A,   101",
            "ZERO_BASED,  100,  AT,  102",
    })
    void fromAllele(CoordinateSystem coordinateSystem, int start, String ref, int expectEnd) {
        Coordinates fromAllele = Coordinates.ofAllele(coordinateSystem, start, ref);
        Coordinates expected = Coordinates.of(coordinateSystem, start, expectEnd);
        assertEquals(expected, fromAllele);
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   1",
            "ZERO_BASED,  0",
    })
    void breakend(CoordinateSystem coordinateSystem, int start) {
        Coordinates breakend = Coordinates.ofBreakend(coordinateSystem, start, ConfidenceInterval.precise());
        assertThat(breakend.length(), equalTo(0));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   1, 0,  0",
            "ONE_BASED,   1, 1,  1",
            "ONE_BASED,   1, 2,  2",

            "ZERO_BASED,  0, 0,  0",
            "ZERO_BASED,  0, 1,  1",
            "ZERO_BASED,  0, 2,  2",
    })
    void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(Coordinates.length(coordinateSystem, start, end), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // given a coordinate on a contig of length 5
            "ONE_BASED,   1,  5",
            "ONE_BASED,   2,  4",
            "ONE_BASED,   3,  3",
            "ONE_BASED,   4,  2",
            "ONE_BASED,   5,  1",

            "ZERO_BASED,  0,  5",
            "ZERO_BASED,  1,  4",
            "ZERO_BASED,  2,  3",
            "ZERO_BASED,  3,  2",
            "ZERO_BASED,  4,  1",
            "ZERO_BASED,  5,  0",
    })
    void invert(CoordinateSystem coordinateSystem, int pos, int expected) {
        Contig contig = TestContig.of(1, 5);
        assertThat(Coordinates.invertCoordinate(coordinateSystem, contig, pos), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // given a coordinate on a contig of length 5
            "ONE_BASED,   1,  5,   1,  5",
            "ONE_BASED,   2,  4,   2,  4",
            "ONE_BASED,   3,  3,   3,  3",
            "ONE_BASED,   2,  3,   3,  4",
            "ONE_BASED,   4,  5,   1,  2",

            "ZERO_BASED,  0,  5,  0,  5",
            "ZERO_BASED,  1,  4,  1,  4",
            "ZERO_BASED,  2,  3,  2,  3",
            "ZERO_BASED,  3,  4,  1,  2",
            "ZERO_BASED,  4,  5,  0,  1",
            "ZERO_BASED,  2,  4,  1,  3",
    })
    void invertCoordinates(CoordinateSystem coordinateSystem, int start, int end, int exptStart, int exptEnd) {
        Contig contig = TestContig.of(1, 5);
        Coordinates initial = Coordinates.of(coordinateSystem, start, end);
        Coordinates expected = Coordinates.of(coordinateSystem, exptStart, exptEnd);
        assertThat(initial.invert(contig), equalTo(expected));
    }

    @Nested
    class Overlap {
        @ParameterizedTest
        @CsvSource({
                "ONE_BASED,   1, 1,  ONE_BASED,   1, 1,    true",
                "ONE_BASED,   1, 1,  ZERO_BASED,  0, 1,    true",
                "ONE_BASED,   1, 5,  ONE_BASED,   5, 7,    true",
                "ONE_BASED,   5, 5,  ONE_BASED,   5, 7,    true",
                "ONE_BASED,   1, 5,  ONE_BASED,   6, 8,    false",
                "ZERO_BASED,  1, 5,  ZERO_BASED,  1, 5,    true",
                "ZERO_BASED,  1, 5,  ZERO_BASED,  4, 8,    true",
                "ZERO_BASED,  1, 5,  ZERO_BASED,  5, 8,    false",
                "ZERO_BASED,  0, 1,  ONE_BASED,   1, 1,    true",
        })
        void overlap(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty regions
                "ONE_BASED,   1, 0,  ONE_BASED,   1, 0,  true",
                "ONE_BASED,   1, 0,  ZERO_BASED,  0, 0,  true",

                "ZERO_BASED,  0, 0,  ONE_BASED,   1, 0,  true",
                "ZERO_BASED,  0, 0,  ZERO_BASED,  0, 0,  true",
        })
        void overlap_emptyRegions(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty intervals on immediate boundaries
                "ONE_BASED,   1, 0,  ONE_BASED,   1, 1,    false",
                "ONE_BASED,   2, 1,  ONE_BASED,   1, 1,    false",
                "ONE_BASED,   1, 0,  ZERO_BASED,  0, 1,    false",
                "ONE_BASED,   2, 1,  ZERO_BASED,  0, 1,    false",

                "ZERO_BASED,  0, 0,  ONE_BASED,   1, 1,    false",
                "ZERO_BASED,  1, 1,  ONE_BASED,   1, 1,    false",
                "ZERO_BASED,  0, 0,  ZERO_BASED,  0, 1,    false",
                "ZERO_BASED,  1, 1,  ZERO_BASED,  0, 1,    false",
        })
        void overlap_excludesEmptyIntervalsOnBoundaries(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource({
                // empty
                "ZERO_BASED,  0, 0,  ZERO_BASED,  0, 1,    false",
                "ZERO_BASED,  0, 1,  ZERO_BASED,  0, 0,    false",

                // full
                "ZERO_BASED,  0, 5,  ZERO_BASED,  3, 8,    true",
                "ZERO_BASED,  3, 8,  ZERO_BASED,  0, 5,    true",
        })
        void overlap_isTransitive(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
            assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
        }
    }

    @Nested
    class Validate {

        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "ONE_BASED,   1,  0",
                "ONE_BASED,   1,  1",
                "ONE_BASED,   1,  5",

                "ZERO_BASED,  0,  0",
                "ZERO_BASED,  0,  1",
                "ZERO_BASED,  0,  5",
        })
        void validCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            assertDoesNotThrow(() -> Coordinates.validateCoordinates(coordinateSystem, start, end));
        }


        @ParameterizedTest
        @CsvSource({
                // given a coordinate on a contig of length 5
                "ONE_BASED,   1,  -1",
                "ONE_BASED,   0,  1",
                "ONE_BASED,   0,  0",
                "ONE_BASED,   5,  3",

                "ZERO_BASED,  5,  4",
                "ZERO_BASED,  0,  -1",
                "ZERO_BASED,  -1,  0",
        })
        void invalidCoordinates(CoordinateSystem coordinateSystem, int start, int end) {
            assertThrows(InvalidCoordinatesException.class, () -> Coordinates.validateCoordinates(coordinateSystem, start, end));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "ONE_BASED,   -1",
        "ZERO_BASED,   0",
    })
    void endDelta(CoordinateSystem coordinateSystem, int expected) {
        assertThat(Coordinates.endDelta(coordinateSystem), equalTo(expected));
    }

    @Nested
    class DistanceTo {

        @ParameterizedTest
        @CsvSource({
                // overlapping regions, thus distance is 0
                "ONE_BASED,   1, 2,   ONE_BASED,   2, 3,    0",
                "ONE_BASED,   1, 2,   ZERO_BASED,  1, 3,    0",

                "ZERO_BASED,  0, 2,   ONE_BASED,   2, 3,    0",
                "ZERO_BASED,  0, 2,   ZERO_BASED,  1, 3,    0",
        })
        void overlapping(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // adjacent, thus distance is 0
                "ONE_BASED,   1, 2,   ONE_BASED,   3, 4,    0",
                "ONE_BASED,   1, 2,   ZERO_BASED,  2, 4,    0",

                "ZERO_BASED,  0, 2,   ONE_BASED,   3, 4,    0",
                "ZERO_BASED,  0, 2,   ZERO_BASED,  2, 4,    0",
        })
        void adjacent(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }

        @ParameterizedTest
        @CsvSource({
                // all possible combinations of coordinate systems
                "ONE_BASED,   1, 2,   ONE_BASED,   5, 6,    2",
                "ONE_BASED,   1, 2,   ZERO_BASED,  4, 6,    2",

                "ZERO_BASED,  0, 2,   ONE_BASED,   5, 6,    2",
                "ZERO_BASED,  0, 2,   ZERO_BASED,  4, 6,    2",
        })
        void distanceIsTwo(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
            assertThat(Coordinates.distanceAToB(x, xStart, xEnd, y, yStart, yEnd), is(expected));
            assertThat(Coordinates.distanceAToB(y, yStart, yEnd, x, xStart, xEnd), is(-expected));
        }
    }

    @ParameterizedTest
    @CsvSource({
            // no overlap
            "ONE_BASED, 1, 2,   ONE_BASED,  8, 9,    0",
            "ONE_BASED, 1, 2,   ZERO_BASED, 8, 9,    0",

            // partial overlap, transitive
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 2, 3,   ONE_BASED,  1, 2,    1",
            "ONE_BASED, 1, 3,   ONE_BASED,  2, 4,    2",
            "ONE_BASED, 2, 4,   ONE_BASED,  1, 3,    2",

            // complete overlap
            "ONE_BASED, 1, 5,   ONE_BASED,  1, 5,    5",
            "ONE_BASED, 1, 5,   ZERO_BASED, 0, 5,    5",

            // multiple systems
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 1, 2,   ZERO_BASED, 1, 3,    1",

            "ZERO_BASED, 0, 2,   ONE_BASED,  2, 3,    1",
            "ZERO_BASED, 0, 3,   ONE_BASED,  2, 3,    2",
            "ZERO_BASED, 0, 2,   ZERO_BASED, 1, 3,    1",
    })
    void overlapLength(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
        assertThat(Coordinates.overlapLength(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // no overlap
            "ONE_BASED, 1, 2,   ONE_BASED,  8, 9,    0",
            "ONE_BASED, 1, 2,   ZERO_BASED, 8, 9,    0",

            // partial overlap, transitive
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 2, 3,   ONE_BASED,  1, 2,    1",
            "ONE_BASED, 1, 3,   ONE_BASED,  2, 4,    2",
            "ONE_BASED, 2, 4,   ONE_BASED,  1, 3,    2",

            // complete overlap
            "ONE_BASED, 1, 5,   ONE_BASED,  1, 5,    5",
            "ONE_BASED, 1, 5,   ZERO_BASED, 0, 5,    5",

            // multiple systems
            "ONE_BASED, 1, 2,   ONE_BASED,  2, 3,    1",
            "ONE_BASED, 1, 2,   ZERO_BASED, 1, 3,    1",

            "ZERO_BASED, 0, 2,   ONE_BASED,  2, 3,    1",
            "ZERO_BASED, 0, 3,   ONE_BASED,  2, 3,    2",
            "ZERO_BASED, 0, 2,   ZERO_BASED, 1, 3,    1",
    })
    void overlapLengthCoordinates(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, int expected) {
        assertThat(Coordinates.of(x, xStart, xEnd).overlapLength(Coordinates.of(y, yStart, yEnd)), equalTo(expected));
    }

    @Test
    void comparatorNaturalOrderTest() {
        Coordinates first = Coordinates.of(CoordinateSystem.zeroBased(), 0, 5);
        Coordinates second = Coordinates.of(CoordinateSystem.oneBased(), 2, 5);
        Coordinates third = Coordinates.of(CoordinateSystem.zeroBased(), 2, 4);

        List<Coordinates> sorted = Stream.of(second, first, third).sorted(Coordinates.naturalOrder()).toList();
        assertThat(sorted, equalTo(List.of(first, second, third)));
    }

    @Test
    void comparableTest() {
        Coordinates first = Coordinates.of(CoordinateSystem.zeroBased(), 0, 5);
        Coordinates second = Coordinates.of(CoordinateSystem.oneBased(), 2, 5);
        Coordinates third = Coordinates.of(CoordinateSystem.zeroBased(), 2, 4);

        List<Coordinates> sorted = Stream.of(second, first, third).sorted().toList();
        assertThat(sorted, equalTo(List.of(first, second, third)));
    }
}
