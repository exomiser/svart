package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RegionTest {

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 3, 3,    ZERO_BASED, 1, 2,  false",
            "ONE_BASED, 3, 3,    ONE_BASED, 2, 2,  false",

            "ONE_BASED, 3, 3,    ZERO_BASED, 2, 3,  true",
            "ONE_BASED, 3, 3,    ONE_BASED, 3, 3,  true",

            "ONE_BASED, 3, 3,    ZERO_BASED, 3, 4,  false",
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
            "ZERO_BASED,  2, 3,   2, false",
            "ZERO_BASED,  2, 3,   3, true",
            "ZERO_BASED,  2, 3,   4, false",

            "ONE_BASED, 3, 3,   2, false",
            "ONE_BASED, 3, 3,   3, true",
            "ONE_BASED, 3, 3,   4, false",
    })
    public void contains_position(CoordinateSystem coordinateSystem, int start, int end, int pos, boolean expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        assertThat(region.contains(pos), equalTo(expected));
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
            "ONE_BASED, 4, 5,   ONE_BASED, 1, 2,  -1",
            "ONE_BASED, 4, 5,   ONE_BASED, 2, 3,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 3, 4,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 4, 5,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 5, 6,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 6, 7,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 7, 8,  1",
    })
    public void distanceTo(CoordinateSystem coordinateSystem, int start, int end,
                           CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                           int expected) {
        TestRegion region = TestRegion.of(coordinateSystem, start, end);
        TestRegion query = TestRegion.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.distanceTo(query), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED,    2, 2,   0",
            "ONE_BASED, 2, 1,   0",

            "ZERO_BASED,    1, 2,   1",
            "ONE_BASED, 2, 2,   1",

            "ONE_BASED, 2, 3,   2",
            "ONE_BASED, 2, 4,   3",
    })
    public void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(TestRegion.of(coordinateSystem,start, end).length(), equalTo(expected));
    }
}
