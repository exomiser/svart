package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CoordinatesTest {

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 1, 1,  FULLY_CLOSED, 1, 1,    true",
            "FULLY_CLOSED, 1, 1,  LEFT_OPEN,    0, 1,    true",
            "FULLY_CLOSED, 1, 1,  RIGHT_OPEN,   1, 2,    true",
            "FULLY_CLOSED, 1, 1,  FULLY_OPEN,   0, 2,    true",
            "FULLY_CLOSED, 1, 5,  FULLY_CLOSED, 5, 7,    true",
            "FULLY_CLOSED, 5, 5,  FULLY_CLOSED, 5, 7,    true",
            "FULLY_CLOSED, 1, 5,  FULLY_CLOSED, 6, 8,    false",
            "LEFT_OPEN,    1, 5,  LEFT_OPEN,    1, 5,    true",
            "LEFT_OPEN,    1, 5,  LEFT_OPEN,    4, 8,    true",
            "LEFT_OPEN,    1, 5,  LEFT_OPEN,    5, 8,    false",
            "RIGHT_OPEN,   1, 5,  RIGHT_OPEN,   4, 8,    true",
            "RIGHT_OPEN,   1, 5,  RIGHT_OPEN,   5, 8,    false",
            "RIGHT_OPEN,   1, 5,  LEFT_OPEN,    3, 8,    true",
            "RIGHT_OPEN,   1, 5,  LEFT_OPEN,    5, 8,    false",
            "LEFT_OPEN,    0, 1,  FULLY_CLOSED, 1, 1,    true",
            "FULLY_OPEN,   0, 2,  FULLY_CLOSED, 1, 1,    true",
    })
    public void overlap(CoordinateSystem x, int xStart, int xEnd, CoordinateSystem y, int yStart, int yEnd, boolean expected) {
        assertThat(Coordinates.overlap(x, xStart, xEnd, y, yStart, yEnd), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // empty regions
            "FULLY_CLOSED, 1, 0,  FULLY_CLOSED, 1, 0,    true",
            "FULLY_CLOSED, 1, 0,  LEFT_OPEN,    0, 0,    true",
            "FULLY_CLOSED, 1, 0,  RIGHT_OPEN,   1, 1,    true",
            "FULLY_CLOSED, 1, 0,  FULLY_OPEN,   0, 1,    true",

            "LEFT_OPEN,    0, 0,  FULLY_CLOSED, 1, 0,    true",
            "LEFT_OPEN,    0, 0,  LEFT_OPEN,    0, 0,    true",
            "LEFT_OPEN,    0, 0,  RIGHT_OPEN,   1, 1,    true",
            "LEFT_OPEN,    0, 0,  FULLY_OPEN,   0, 1,    true",

            "RIGHT_OPEN,    1, 1,  FULLY_CLOSED, 1, 0,    true",
            "RIGHT_OPEN,    1, 1,  LEFT_OPEN,    0, 0,    true",
            "RIGHT_OPEN,    1, 1,  RIGHT_OPEN,   1, 1,    true",
            "RIGHT_OPEN,    1, 1,  FULLY_OPEN,   0, 1,    true",

            "FULLY_OPEN,    0, 1,  FULLY_CLOSED, 1, 0,    true",
            "FULLY_OPEN,    0, 1,  LEFT_OPEN,    0, 0,    true",
            "FULLY_OPEN,    0, 1,  RIGHT_OPEN,   1, 1,    true",
            "FULLY_OPEN,    0, 1,  FULLY_OPEN,   0, 1,    true",
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
            "FULLY_CLOSED,  1, 0,  RIGHT_OPEN,   1, 2,    true",
            "FULLY_CLOSED,  2, 1,  RIGHT_OPEN,   1, 2,    true",
            "FULLY_CLOSED,  1, 0,  FULLY_OPEN,   0, 2,    true",
            "FULLY_CLOSED,  2, 1,  FULLY_OPEN,   0, 2,    true",

            "LEFT_OPEN,     0, 0,  FULLY_CLOSED, 1, 1,    true",
            "LEFT_OPEN,     1, 1,  FULLY_CLOSED, 1, 1,    true",
            "LEFT_OPEN,     0, 0,  LEFT_OPEN,    0, 1,    true",
            "LEFT_OPEN,     1, 1,  LEFT_OPEN,    0, 1,    true",
            "LEFT_OPEN,     0, 0,  RIGHT_OPEN,   1, 2,    true",
            "LEFT_OPEN,     1, 1,  RIGHT_OPEN,   1, 2,    true",
            "LEFT_OPEN,     0, 0,  FULLY_OPEN,   0, 2,    true",
            "LEFT_OPEN,     1, 1,  FULLY_OPEN,   0, 2,    true",

            "RIGHT_OPEN,    1, 1,  FULLY_CLOSED, 1, 1,    true",
            "RIGHT_OPEN,    2, 2,  FULLY_CLOSED, 1, 1,    true",
            "RIGHT_OPEN,    1, 1,  LEFT_OPEN,    0, 1,    true",
            "RIGHT_OPEN,    2, 2,  LEFT_OPEN,    0, 1,    true",
            "RIGHT_OPEN,    1, 1,  RIGHT_OPEN,   1, 2,    true",
            "RIGHT_OPEN,    2, 2,  RIGHT_OPEN,   1, 2,    true",
            "RIGHT_OPEN,    1, 1,  FULLY_OPEN,   0, 2,    true",
            "RIGHT_OPEN,    2, 2,  FULLY_OPEN,   0, 2,    true",

            "FULLY_OPEN,    0, 1,  FULLY_CLOSED, 1, 1,    true",
            "FULLY_OPEN,    1, 2,  FULLY_CLOSED, 1, 1,    true",
            "FULLY_OPEN,    0, 1,  LEFT_OPEN,    0, 1,    true",
            "FULLY_OPEN,    1, 2,  LEFT_OPEN,    0, 1,    true",
            "FULLY_OPEN,    0, 1,  RIGHT_OPEN,   1, 2,    true",
            "FULLY_OPEN,    1, 2,  RIGHT_OPEN,   1, 2,    true",
            "FULLY_OPEN,    0, 1,  FULLY_OPEN,   0, 2,    true",
            "FULLY_OPEN,    1, 2,  FULLY_OPEN,   0, 2,    true",
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

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 1, 0,  0",
            "FULLY_CLOSED, 1, 1,  1",
            "FULLY_CLOSED, 1, 2,  2",

            "LEFT_OPEN,    0, 0,  0",
            "LEFT_OPEN,    0, 1,  1",
            "LEFT_OPEN,    0, 2,  2",

            "RIGHT_OPEN,   1, 1,  0",
            "RIGHT_OPEN,   1, 2,  1",
            "RIGHT_OPEN,   1, 3,  2",

            "FULLY_OPEN,   0, 1,  0",
            "FULLY_OPEN,   0, 2,  1",
            "FULLY_OPEN,   0, 3,  2",
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

            "RIGHT_OPEN,   1,  6",
            "RIGHT_OPEN,   2,  5",
            "RIGHT_OPEN,   3,  4",
            "RIGHT_OPEN,   4,  3",
            "RIGHT_OPEN,   5,  2",
            "RIGHT_OPEN,   6,  1",

            "FULLY_OPEN,   0,  6",
            "FULLY_OPEN,   1,  5",
            "FULLY_OPEN,   2,  4",
            "FULLY_OPEN,   3,  3",
            "FULLY_OPEN,   4,  2",
            "FULLY_OPEN,   5,  1",
            "FULLY_OPEN,   6,  0",
    })
    public void invert(CoordinateSystem coordinateSystem, int pos, int expected) {
        Contig contig = TestContig.of(1, 5);
        assertThat(Coordinates.invertPosition(coordinateSystem, pos, contig), equalTo(expected));
    }
}