package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RegionTest {

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 3, 3,   FULLY_OPEN, 1, 3,  false",
            "ONE_BASED, 3, 3,   ZERO_BASED, 1, 2,  false",
            "ONE_BASED, 3, 3,   RIGHT_OPEN, 2, 3,  false",
            "ONE_BASED, 3, 3,    ONE_BASED, 2, 2,  false",

            "ONE_BASED, 3, 3,   FULLY_OPEN, 2, 4,  true",
            "ONE_BASED, 3, 3,   ZERO_BASED, 2, 3,  true",
            "ONE_BASED, 3, 3,   RIGHT_OPEN, 3, 4,  true",
            "ONE_BASED, 3, 3,    ONE_BASED, 3, 3,  true",

            "ONE_BASED, 3, 3,   FULLY_OPEN, 3, 5,  false",
            "ONE_BASED, 3, 3,   ZERO_BASED, 3, 4,  false",
            "ONE_BASED, 3, 3,   RIGHT_OPEN, 4, 5,  false",
            "ONE_BASED, 3, 3,    ONE_BASED, 4, 4,  false",

            "ONE_BASED, 3, 4,    ONE_BASED, 2, 3,  false",
            "ONE_BASED, 3, 4,    ONE_BASED, 3, 4,  true",
            "ONE_BASED, 3, 4,    ONE_BASED, 4, 5,  false",
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

            "ZERO_BASED, 2, 3,   2, false",
            "ZERO_BASED, 2, 3,   3, true",
            "ZERO_BASED, 2, 3,   4, false",

            "RIGHT_OPEN, 3, 4,   2, false",
            "RIGHT_OPEN, 3, 4,   3, true",
            "RIGHT_OPEN, 3, 4,   4, false",

            " ONE_BASED, 3, 3,   2, false",
            " ONE_BASED, 3, 3,   3, true",
            " ONE_BASED, 3, 3,   4, false",
    })
    public void contains_position(CoordinateSystem coordinateSystem, int start, int end,
                                  int pos, boolean expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        Position query = Position.of(pos);

        assertThat(region.contains(query), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 3, 4,   ONE_BASED, 1, 2,  false",
            "ONE_BASED, 3, 4,   ONE_BASED, 2, 3,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 3, 4,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 4, 5,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 5, 5,  false",
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
            "FULLY_OPEN, 1, 2,   0",
            "ZERO_BASED, 2, 2,   0",
            "RIGHT_OPEN, 2, 2,   0",
            " ONE_BASED, 2, 1,   0",

            "FULLY_OPEN, 1, 3,   1",
            "ZERO_BASED, 1, 2,   1",
            "RIGHT_OPEN, 2, 3,   1",
            " ONE_BASED, 2, 2,   1",

            " ONE_BASED, 2, 3,   2",
            " ONE_BASED, 2, 4,   3",
    })
    public void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(TestRegion.of(coordinateSystem,start, end).length(), equalTo(expected));
    }
}
