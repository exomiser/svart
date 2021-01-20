package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RegionTest {

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 3, 3,   FULLY_OPEN, 1, 3,  false",
            "FULLY_CLOSED, 3, 3,   LEFT_OPEN, 1, 2,  false",
            "FULLY_CLOSED, 3, 3,   RIGHT_OPEN, 2, 3,  false",
            "FULLY_CLOSED, 3, 3,    FULLY_CLOSED, 2, 2,  false",

            "FULLY_CLOSED, 3, 3,   FULLY_OPEN, 2, 4,  true",
            "FULLY_CLOSED, 3, 3,   LEFT_OPEN, 2, 3,  true",
            "FULLY_CLOSED, 3, 3,   RIGHT_OPEN, 3, 4,  true",
            "FULLY_CLOSED, 3, 3,    FULLY_CLOSED, 3, 3,  true",

            "FULLY_CLOSED, 3, 3,   FULLY_OPEN, 3, 5,  false",
            "FULLY_CLOSED, 3, 3,   LEFT_OPEN, 3, 4,  false",
            "FULLY_CLOSED, 3, 3,   RIGHT_OPEN, 4, 5,  false",
            "FULLY_CLOSED, 3, 3,    FULLY_CLOSED, 4, 4,  false",

            "FULLY_CLOSED, 3, 4,    FULLY_CLOSED, 2, 3,  false",
            "FULLY_CLOSED, 3, 4,    FULLY_CLOSED, 3, 4,  true",
            "FULLY_CLOSED, 3, 4,    FULLY_CLOSED, 4, 5,  false",
    })
    public void contains_region(CoordinateSystem coordinateSystem, int start, int end,
                                CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                                boolean expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        TestRegion query = TestRegion.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.contains(query), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "FULLY_OPEN, 2, 4,   2, false",
            "FULLY_OPEN, 2, 4,   3, true",
            "FULLY_OPEN, 2, 4,   4, false",

            "LEFT_OPEN,  2, 3,   2, false",
            "LEFT_OPEN,  2, 3,   3, true",
            "LEFT_OPEN,  2, 3,   4, false",

            "RIGHT_OPEN, 3, 4,   2, false",
            "RIGHT_OPEN, 3, 4,   3, true",
            "RIGHT_OPEN, 3, 4,   4, false",

            "FULLY_CLOSED, 3, 3,   2, false",
            "FULLY_CLOSED, 3, 3,   3, true",
            "FULLY_CLOSED, 3, 3,   4, false",
    })
    public void contains_position(CoordinateSystem coordinateSystem, int start, int end, int pos, boolean expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        assertThat(region.contains(Position.of(pos)), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED, 3, 4,   FULLY_CLOSED, 1, 2,  false",
            "FULLY_CLOSED, 3, 4,   FULLY_CLOSED, 2, 3,  true",
            "FULLY_CLOSED, 3, 4,   FULLY_CLOSED, 3, 4,  true",
            "FULLY_CLOSED, 3, 4,   FULLY_CLOSED, 4, 5,  true",
            "FULLY_CLOSED, 3, 4,   FULLY_CLOSED, 5, 5,  false",
    })
    public void overlapsWith(CoordinateSystem coordinateSystem, int start, int end,
                             CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                             boolean expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        TestRegion query = TestRegion.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.overlapsWith(query), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "FULLY_OPEN,   1, 2,   0",
            "LEFT_OPEN,    2, 2,   0",
            "RIGHT_OPEN,   2, 2,   0",
            "FULLY_CLOSED, 2, 1,   0",

            "FULLY_OPEN,   1, 3,   1",
            "LEFT_OPEN,    1, 2,   1",
            "RIGHT_OPEN,   2, 3,   1",
            "FULLY_CLOSED, 2, 2,   1",

            "FULLY_CLOSED, 2, 3,   2",
            "FULLY_CLOSED, 2, 4,   3",
    })
    public void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(TestRegion.of(coordinateSystem,start, end).length(), equalTo(expected));
    }
}
